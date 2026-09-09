package com.alibaba.druid.spring.boot.metrics;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.DruidProperties;
import com.alibaba.druid.spring.boot.ds.DynamicRoutingDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadata;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class DruidMetricsAutoConfigurationTest {

    @Test
    void configuresStatFilterMetadataAndSpringStat() {
        DruidProperties properties = new DruidProperties();
        Properties connectionProperties = new Properties();
        connectionProperties.setProperty("druid.stat.slowSqlMillis", "250");
        connectionProperties.setProperty("druid.stat.mergeSql", "true");
        connectionProperties.setProperty("druid.stat.logSlowSql", "true");
        properties.setConnectionProperties(connectionProperties);
        DruidMetricsAutoConfiguration configuration = new DruidMetricsAutoConfiguration(properties);

        StatFilter filter = configuration.statFilter();
        assertThat(filter.getSlowSqlMillis()).isEqualTo(250L);
        assertThat(filter.isMergeSql()).isTrue();
        assertThat(filter.isLogSlowSql()).isTrue();
        assertThat(configuration.springStat()).isNotNull();
        configuration.validateMetricPrefix();

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setMaxActive(19);
        dataSource.setMinIdle(2);
        dataSource.setValidationQuery("select 1");
        DataSourcePoolMetadataProvider provider = configuration.druidDataSourceMetadataProvider();
        DataSourcePoolMetadata metadata = provider.getDataSourcePoolMetadata(dataSource);
        assertThat(metadata).isInstanceOf(DruidDataSourcePoolMetadata.class);
        assertThat(metadata.getActive()).isZero();
        assertThat(metadata.getIdle()).isZero();
        assertThat(metadata.getMax()).isEqualTo(19);
        assertThat(metadata.getMin()).isEqualTo(2);
        assertThat(metadata.getValidationQuery()).isEqualTo("select 1");
        assertThat(metadata.getDefaultAutoCommit()).isTrue();
        assertThat(provider.getDataSourcePoolMetadata(org.mockito.Mockito.mock(DataSource.class))).isNull();
    }

    @Test
    void rejectsReservedMetricsNamespace() {
        DruidProperties properties = new DruidProperties();
        Properties connectionProperties = new Properties();
        connectionProperties.setProperty("custom", "DRUID.METRICS.invalid");
        properties.setConnectionProperties(connectionProperties);
        DruidMetricsAutoConfiguration configuration = new DruidMetricsAutoConfiguration(properties);
        assertThatIllegalStateException().isThrownBy(configuration::validateMetricPrefix);
    }

    @Test
    void discoversDirectAndDynamicDruidPools() throws Exception {
        DruidProperties properties = new DruidProperties();
        DruidMetricsAutoConfiguration configuration = new DruidMetricsAutoConfiguration(properties);
        DruidDataSource direct = new DruidDataSource();
        DruidDataSource slave = new DruidDataSource();
        DruidDataSource master = new DruidDataSource();
        initialize(direct, "direct_pool");
        initialize(slave, "slave_pool");
        initialize(master, "master_pool");

        DynamicRoutingDataSource routing = new DynamicRoutingDataSource();
        Map<Object, Object> targets = new HashMap<>();
        targets.put("slave", slave);
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(master);
        routing.afterPropertiesSet();

        Map<String, DataSource> dataSources = new HashMap<>();
        dataSources.put("primaryDataSource", direct);
        dataSources.put("routingDataSource", routing);
        @SuppressWarnings("unchecked")
        ObjectProvider<Map<String, DataSource>> provider = org.mockito.Mockito.mock(ObjectProvider.class);
        org.mockito.Mockito.when(provider.getIfAvailable(org.mockito.ArgumentMatchers.any()))
                .thenReturn(dataSources);

        try {
            SimpleMeterRegistry registry = new SimpleMeterRegistry();
            configuration.druidMetrics(provider).bindTo(registry);
            Set<String> pools = registry.getMeters().stream()
                    .map(Meter::getId)
                    .flatMap(id -> id.getTags().stream())
                    .filter(tag -> "pool".equals(tag.getKey()))
                    .map(tag -> tag.getValue())
                    .collect(Collectors.toSet());
            assertThat(pools).contains("primary", "routing-slave", "routing-master");
        } finally {
            direct.close();
            slave.close();
            master.close();
        }
    }

    @Test
    void discoversGenericRoutingDataSourcePools() throws Exception {
        DruidDataSource secondary = new DruidDataSource();
        DruidDataSource defaultPool = new DruidDataSource();
        initialize(secondary, "generic_secondary");
        initialize(defaultPool, "generic_default");
        TestRoutingDataSource routing = new TestRoutingDataSource();
        Map<Object, Object> targets = new HashMap<>();
        targets.put("secondary", secondary);
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(defaultPool);
        routing.afterPropertiesSet();

        Map<String, DataSource> dataSources = Collections.singletonMap("genericDataSource", routing);
        @SuppressWarnings("unchecked")
        ObjectProvider<Map<String, DataSource>> provider = org.mockito.Mockito.mock(ObjectProvider.class);
        org.mockito.Mockito.when(provider.getIfAvailable(org.mockito.ArgumentMatchers.any()))
                .thenReturn(dataSources);
        try {
            SimpleMeterRegistry registry = new SimpleMeterRegistry();
            new DruidMetricsAutoConfiguration(new DruidProperties()).druidMetrics(provider).bindTo(registry);
            Set<String> pools = registry.getMeters().stream()
                    .map(Meter::getId)
                    .flatMap(id -> id.getTags().stream())
                    .filter(tag -> "pool".equals(tag.getKey()))
                    .map(tag -> tag.getValue())
                    .collect(Collectors.toSet());
            assertThat(pools).contains("generic-secondary", "generic-default");
        } finally {
            secondary.close();
            defaultPool.close();
        }
    }

    @Test
    void handlesMissingDataSourceMapAndConnectionProperties() {
        DruidProperties properties = new DruidProperties();
        DruidMetricsAutoConfiguration configuration = new DruidMetricsAutoConfiguration(properties);
        StaticApplicationContext context = new StaticApplicationContext();
        ObjectProvider<Map<String, DataSource>> provider = context.getBeanProvider(
                org.springframework.core.ResolvableType.forClassWithGenerics(Map.class, String.class, DataSource.class));
        assertThat(configuration.druidMetrics(provider)).isNotNull();
        assertThat(configuration.statFilter()).isNotNull();
        configuration.validateMetricPrefix();
        context.close();
    }

    private void initialize(DruidDataSource dataSource, String database) throws Exception {
        dataSource.setUrl("jdbc:h2:mem:" + database + ";DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setValidationQuery("select 1");
        dataSource.init();
    }

    private static final class TestRoutingDataSource extends AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() {
            return null;
        }
    }
}
