package com.alibaba.druid.spring.boot.metrics;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.DruidProperties;
import com.alibaba.druid.spring.boot.ds.DynamicRoutingDataSource;
import com.alibaba.druid.support.spring.stat.SpringStat;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceUnwrapper;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@AutoConfiguration(after = {MetricsAutoConfiguration.class, DataSourceAutoConfiguration.class,
		SimpleMetricsExportAutoConfiguration.class})
@ConditionalOnClass(io.micrometer.core.instrument.MeterRegistry.class)
@ConditionalOnProperty(prefix = DruidMetricsAutoConfiguration.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DruidProperties.class)
public class DruidMetricsAutoConfiguration {

	public static final String PREFIX = "druid.metrics";
	private static final String DATASOURCE_BEAN_SUFFIX = "DataSource";

	private final DruidProperties druidProperties;

	public DruidMetricsAutoConfiguration(DruidProperties druidProperties) {
		this.druidProperties = druidProperties;
	}

	@Bean
	@ConditionalOnMissingBean
	public DataSourcePoolMetadataProvider druidDataSourceMetadataProvider() {
		return dataSource -> {
			DruidDataSource unwrapped = DataSourceUnwrapper.unwrap(dataSource, DruidDataSource.class);
			if (unwrapped != null) {
				return new DruidDataSourcePoolMetadata(unwrapped);
			}
			return null;
		};
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = DruidProperties.PREFIX, name = "stat.enabled", matchIfMissing = true)
	public StatFilter statFilter() {
		StatFilter statFilter = new StatFilter();
		Properties connectionProperties = druidProperties.getConnectionProperties();
		if (connectionProperties != null) {
			String slowSqlMillis = connectionProperties.getProperty("druid.stat.slowSqlMillis");
			if (NumberUtils.isParsable(slowSqlMillis)) {
				statFilter.setSlowSqlMillis(Long.parseLong(slowSqlMillis));
			}
			String mergeSql = connectionProperties.getProperty("druid.stat.mergeSql");
			statFilter.setMergeSql(Boolean.parseBoolean(mergeSql));
			String logSlowSql = connectionProperties.getProperty("druid.stat.logSlowSql");
			if (StringUtils.isNotBlank(logSlowSql)) {
				statFilter.setLogSlowSql(Boolean.parseBoolean(logSlowSql));
			}
		}
		return statFilter;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(io.micrometer.core.instrument.MeterRegistry.class)
	public DruidMetrics druidMetrics(ObjectProvider<Map<String, DataSource>> dataSourcesProvider) {
		Map<String, DataSource> dataSourceMap = dataSourcesProvider.getIfAvailable(Collections::emptyMap);
		Map<String, DruidDataSource> druidDataSourceMap = new LinkedHashMap<>();
		for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
			DataSource dataSource = entry.getValue();
			Map<String, DruidDataSource> unwrapped = unwrapAllDruidDataSources(entry.getKey(), dataSource);
			druidDataSourceMap.putAll(unwrapped);
		}
		return new DruidMetrics(druidDataSourceMap);
	}

	@Bean(name = "druidSpringStat")
	@ConditionalOnMissingBean(name = "druidSpringStat")
	public SpringStat springStat() {
		return new SpringStat();
	}

	@PostConstruct
	public void validateMetricPrefix() {
		if (druidProperties.getConnectionProperties() != null) {
			String connectionProperties = druidProperties.getConnectionProperties().toString();
			if (StringUtils.containsIgnoreCase(connectionProperties, "druid.metrics")) {
				throw new IllegalStateException(
						"druid.metrics namespace is reserved for druid-metrics-prometheus auto-configuration; "
								+ "do not reuse it inside connectionProperties.");
			}
		}
	}

	private static Map<String, DruidDataSource> unwrapAllDruidDataSources(String beanName, DataSource dataSource) {
		Map<String, DruidDataSource> result = new LinkedHashMap<>();
		String poolName = normalizePoolName(beanName);

		if (dataSource instanceof DynamicRoutingDataSource dynamicRouting) {
			Map<Object, DataSource> targets = extractResolvedDataSources(dynamicRouting);
			DataSource defaultTarget = extractDefaultTargetDataSource(dynamicRouting);
			for (Map.Entry<Object, DataSource> entry : targets.entrySet()) {
				String subName = entry.getKey().toString();
				DruidDataSource druid = DataSourceUnwrapper.unwrap(entry.getValue(), DruidDataSource.class);
				if (druid != null) {
					result.put(poolName + "-" + subName, druid);
				}
			}
			if (defaultTarget != null) {
				DruidDataSource defaultDruid = DataSourceUnwrapper.unwrap(defaultTarget, DruidDataSource.class);
				if (defaultDruid != null && !result.containsValue(defaultDruid)) {
					result.put(poolName + "-master", defaultDruid);
				}
			}
			return result;
		}

		if (dataSource instanceof AbstractRoutingDataSource abstractRouting) {
			Map<Object, DataSource> targets = extractResolvedDataSources(abstractRouting);
			DataSource defaultTarget = extractDefaultTargetDataSource(abstractRouting);
			for (Map.Entry<Object, DataSource> entry : targets.entrySet()) {
				String subName = entry.getKey().toString();
				DruidDataSource druid = DataSourceUnwrapper.unwrap(entry.getValue(), DruidDataSource.class);
				if (druid != null) {
					result.put(poolName + "-" + subName, druid);
				}
			}
			if (defaultTarget != null) {
				DruidDataSource defaultDruid = DataSourceUnwrapper.unwrap(defaultTarget, DruidDataSource.class);
				if (defaultDruid != null && !result.containsValue(defaultDruid)) {
					result.put(poolName + "-default", defaultDruid);
				}
			}
			if (!result.isEmpty()) {
				return result;
			}
		}

		DruidDataSource direct = DataSourceUnwrapper.unwrap(dataSource, DruidDataSource.class);
		if (direct != null) {
			result.put(poolName, direct);
		}
		return result;
	}

	private static Map<Object, DataSource> extractResolvedDataSources(AbstractRoutingDataSource routing) {
		try {
			Field field = AbstractRoutingDataSource.class.getDeclaredField("resolvedDataSources");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<Object, DataSource> map = (Map<Object, DataSource>) field.get(routing);
			return map == null ? Collections.emptyMap() : new HashMap<>(map);
		} catch (ReflectiveOperationException e) {
			return Collections.emptyMap();
		}
	}

	private static DataSource extractDefaultTargetDataSource(AbstractRoutingDataSource routing) {
		try {
			Field field = AbstractRoutingDataSource.class.getDeclaredField("resolvedDefaultDataSource");
			field.setAccessible(true);
			Object value = field.get(routing);
			return value instanceof DataSource ds ? ds : null;
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	private static String normalizePoolName(String beanName) {
		String stripped = beanName;
		if (StringUtils.endsWithIgnoreCase(stripped, DATASOURCE_BEAN_SUFFIX)) {
			stripped = stripped.substring(0, stripped.length() - DATASOURCE_BEAN_SUFFIX.length());
		}
		if (StringUtils.isBlank(stripped)) {
			stripped = beanName;
		}
		return stripped;
	}

}
