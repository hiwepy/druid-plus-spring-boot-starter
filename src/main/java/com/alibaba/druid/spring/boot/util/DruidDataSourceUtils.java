package com.alibaba.druid.spring.boot.util;

import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.jdbc.DatabaseDriver;

import com.alibaba.druid.filter.config.ConfigFilter;
import com.alibaba.druid.filter.encoding.EncodingConvertFilter;
import com.alibaba.druid.filter.logging.CommonsLogFilter;
import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.filter.logging.Log4jFilter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.ds.DruidDataSourceProperties;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.druid.wall.WallFilter;

/**
 * <p>Auto-configuration for DruidDataSourceUtils.</p>
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class DruidDataSourceUtils {

	/**
	 * <p>Create data source.</p>
	 * @param druidProperties
	 * @return the result
	 */
	public static <T extends DataSource> DruidDataSource createDataSource( DruidDataSourceProperties druidProperties ) {
		
		DataSourceProperties tmProperties = new DataSourceProperties();
		
		tmProperties.setName(druidProperties.getName());
		tmProperties.setType(com.alibaba.druid.pool.DruidDataSource.class);
		// 这一项可配可不配，如果不配置druid会根据url自动识别dbType，然后选择相应的driverClassName
		tmProperties.setDriverClassName(druidProperties.getDriverClassName());
		// jdbcUrl: 连接数据库的url
		tmProperties.setUrl(druidProperties.getUrl());
		// username: 连接数据库的用户名
		tmProperties.setUsername(druidProperties.getUsername());
		// password: 连接数据库的密码
		tmProperties.setPassword(druidProperties.getPassword());
		
		// 创建 DruidDataSource 数据源对象
		DruidDataSource dataSource = createDataSource(tmProperties, tmProperties.getType());
		// 配置 Druid数据源
		configureProperties(druidProperties, dataSource);

		return dataSource;
	}

	@SuppressWarnings("unchecked")
	/**
	 * <p>Create data source.</p>
	 * @param properties
	 * @param type
	 * @return the result
	 */
	public static <T> T createDataSource(DataSourceProperties properties, Class<? extends DataSource> type) {
		return (T) properties.initializeDataSourceBuilder().type(type).build();
	}

	/*
	 * DruidDataSource配置属性列表：
	 * https://github.com/alibaba/druid/wiki/DruidDataSource%E9%85%8D%E7%BD%AE%E5%B1%9E%E6%80%A7%E5%88%97%E8%A1%A8
	 */
	public static void configureProperties(DruidDataSourceProperties druidProperties, DruidDataSource dataSource) {
		
		// 配置这个属性的意义在于，如果存在多个数据源，监控的时候可以通过名字来区分开来。如果没有配置，将会生成一个名字，格式是：”DataSource-” +
		// System.identityHashCode(this)
		if (StringUtils.isNotEmpty(druidProperties.getName())) {
			dataSource.setName(druidProperties.getName());
		}
		
    	/**
		 * 批量设置参数
		 */
		PropertyMapper map = PropertyMapper.get().alwaysApplying(new PropertyMapper.SourceOperator() {
			@Override
			public <T> PropertyMapper.Source<T> apply(PropertyMapper.Source<T> source) {
				return source.when(Objects::nonNull);
			}
		});

		// druid 连接池参数
		//dataSource.configFromPropety(druidProperties.toProperties());
		
		// druid 连接池参数
		map.from(druidProperties.isAccessToUnderlyingConnectionAllowed()).to(dataSource::setAccessToUnderlyingConnectionAllowed);
		map.from(druidProperties.isAsyncCloseConnectionEnable()).to(dataSource::setAsyncCloseConnectionEnable);
		map.from(druidProperties.isAsyncInit()).to(dataSource::setAsyncInit);
		map.from(druidProperties.isBreakAfterAcquireFailure()).to(dataSource::setBreakAfterAcquireFailure);
		map.from(druidProperties.isCheckExecuteTime()).to(dataSource::setCheckExecuteTime);
		map.from(druidProperties.isClearFiltersEnable()).to(dataSource::setClearFiltersEnable);
		map.from(druidProperties.getConnectionErrorRetryAttempts()).to(dataSource::setConnectionErrorRetryAttempts);
		map.from(druidProperties.getConnectionInitSqls()).to(dataSource::setConnectionInitSqls);
		// 额外的链接参数
		map.from(druidProperties.getConnectionProperties()).to(dataSource::setConnectProperties);
		map.from(JdbcUtils.getDbType(druidProperties.getUrl(), null)).to(dataSource::setDbType);
		map.from(druidProperties.isDefaultAutoCommit()).to(dataSource::setDefaultAutoCommit);
		map.from(druidProperties.getDefaultCatalog()).to(dataSource::setDefaultCatalog);
		map.from(druidProperties.isDefaultReadOnly()).to(dataSource::setDefaultReadOnly);
		map.from(druidProperties.getDefaultTransactionIsolation()).to(dataSource::setDefaultTransactionIsolation);
		map.from(druidProperties.isDupCloseLogEnable()).to(dataSource::setDupCloseLogEnable);
		map.from(druidProperties.isFailFast()).to(dataSource::setFailFast);
		/*
		 * Druid的监控统计功能:属性类型是字符串，通过别名的方式配置扩展插件，常用的插件有： #监控统计用的filter:stat
		 * #日志用的filter:log4j #防御SQL注入的filter:wall
		 */
		// 指定过滤器
		map.from(druidProperties.getFilters()).to(filters -> {
			try {
				dataSource.setFilters(filters);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		map.from(druidProperties.isInitExceptionThrow()).to(dataSource::setInitExceptionThrow);
		map.from(druidProperties.isInitGlobalVariants()).to(dataSource::setInitGlobalVariants);
		map.from(druidProperties.isInitVariants()).to(dataSource::setInitVariants);
		// initialSize: 初始化时建立物理连接的个数。初始化发生在显示调用init方法，或者第一次getConnection时
		map.from(druidProperties.getInitialSize()).to(dataSource::setInitialSize);
		// 连接池中的minIdle数量以内的连接，空闲时间超过minEvictableIdleTimeMillis，则会执行keepAlive操作。
		map.from(druidProperties.isKeepAlive()).to(dataSource::setKeepAlive);
		map.from(druidProperties.getKeepAliveBetweenTimeMillis()).to(dataSource::setKeepAliveBetweenTimeMillis);
		map.from(druidProperties.isKillWhenSocketReadTimeout()).to(dataSource::setKillWhenSocketReadTimeout);
		map.from(druidProperties.isLogAbandoned()).to(dataSource::setLogAbandoned);
		map.from(druidProperties.isLogDifferentThread()).to(dataSource::setLogDifferentThread);
		map.from(druidProperties.getLoginTimeout()).to(dataSource::setLoginTimeout);
		
		// maxActive: 最大连接池数量
		map.from(druidProperties.getMaxActive()).to(dataSource::setMaxActive);
		map.from(druidProperties.getMaxCreateTaskCount()).to(dataSource::setMaxCreateTaskCount);
		// 连接最少存活时长和最大存活时长，单位是毫秒，超过上限才会被清理，需要注意满足(maxEvictableIdleTimeMillis-minEvictableIdleTimeMillis>timeBetweenEvictionRunsMillis)的条件
		map.from(druidProperties.getMaxEvictableIdleTimeMillis()).to(dataSource::setMaxEvictableIdleTimeMillis);
		map.from(druidProperties.getMinEvictableIdleTimeMillis()).to(dataSource::setMinEvictableIdleTimeMillis);
		// 是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql5.5以下的版本中没有PSCache功能，建议关闭掉。5.5及以上版本有PSCache，建议开启。
		map.from(druidProperties.isPoolPreparedStatements()).to(dataSource::setPoolPreparedStatements);
		// 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
		map.from(druidProperties.getMaxPoolPreparedStatementPerConnectionSize()).to(dataSource::setMaxOpenPreparedStatements);
		// 获取连接时最大等待时间，单位毫秒。配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，如果需要可以通过配置useUnfairLock属性为true使用非公平锁。
		map.from(druidProperties.getMaxWait()).to(dataSource::setMaxWait);
		map.from(druidProperties.getMaxWaitThreadCount()).to(dataSource::setMaxWaitThreadCount);
		// minIdle: 最小空闲连接数量
		map.from(druidProperties.getMinIdle()).to(dataSource::setMinIdle);
		
		map.from(druidProperties.getNotFullTimeoutRetryCount()).to(dataSource::setNotFullTimeoutRetryCount);
		map.from(druidProperties.getPhyMaxUseCount()).to(dataSource::setPhyMaxUseCount);
		map.from(druidProperties.getPhyTimeoutMillis()).to(dataSource::setPhyTimeoutMillis);
		
		map.from(druidProperties.getQueryTimeout()).to(dataSource::setQueryTimeout);

		// 超过时间限制是否回收
		map.from(druidProperties.isRemoveAbandoned()).to(dataSource::setRemoveAbandoned);
		// 超过时间限制多久触发回收逻辑，单位是毫秒
		map.from(druidProperties.getRemoveAbandonedTimeoutMillis()).to(dataSource::setRemoveAbandonedTimeoutMillis);

		map.from(druidProperties.isResetStatEnable()).to(dataSource::setResetStatEnable);
		map.from(druidProperties.isSharePreparedStatements()).to(dataSource::setSharePreparedStatements);

		// 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
		map.from(druidProperties.isTestOnBorrow()).to(dataSource::setTestOnBorrow);
		// 申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。建议配置为true，不影响性能，并且保证安全性。
		map.from(druidProperties.isTestWhileIdle()).to(dataSource::setTestWhileIdle);
		// 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
		map.from(druidProperties.isTestOnReturn()).to(dataSource::setTestOnReturn);
		map.from(druidProperties.getTimeBetweenConnectErrorMillis()).to(dataSource::setTimeBetweenConnectErrorMillis);
		// 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒;
		// 有两个含义：
		// 1) Destroy线程会检测连接的间隔时间，如果连接空闲时间大于等于minEvictableIdleTimeMillis则关闭物理连接。
		// 2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
		map.from(druidProperties.getTimeBetweenEvictionRunsMillis()).to(dataSource::setTimeBetweenEvictionRunsMillis);
		
		map.from(druidProperties.getTimeBetweenLogStatsMillis()).to(dataSource::setTimeBetweenLogStatsMillis);
		
		map.from(druidProperties.getTransactionQueryTimeout()).to(dataSource::setTransactionQueryTimeout);
		map.from(druidProperties.getTransactionThresholdMillis()).to(dataSource::setTransactionThresholdMillis);
		
		map.from(druidProperties.isUseGlobalDataSourceStat()).to(dataSource::setUseGlobalDataSourceStat);
		map.from(druidProperties.isUseLocalSessionState()).to(dataSource::setUseLocalSessionState);
		map.from(druidProperties.isUseUnfairLock()).to(dataSource::setUseUnfairLock);
	
		if (StringUtils.isNotEmpty(druidProperties.getValidationQuery())) {
			// 用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。
			map.from(druidProperties.getValidationQuery()).to(dataSource::setValidationQuery);
			// 单位：秒，检测连接是否有效的超时时间。底层调用jdbc Statement对象的void setQueryTimeout(int seconds)方法
			map.from(druidProperties.getValidationQueryTimeout()).to(dataSource::setValidationQueryTimeout);
		} else {
			DatabaseDriver databaseDriver = DatabaseDriver.fromJdbcUrl(druidProperties.getUrl());
			String validationQuery = databaseDriver.getValidationQuery();
			if (validationQuery != null) {
				// 用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。
				map.from(validationQuery).to(dataSource::setValidationQuery);
				// 单位：秒，检测连接是否有效的超时时间。底层调用jdbc Statement对象的void setQueryTimeout(int seconds)方法
				map.from(druidProperties.getValidationQueryTimeout()).to(dataSource::setValidationQueryTimeout);
			}
		}
		
	}
	
	public static void configureFilters(DruidDataSource dataSource, ObjectProvider<StatFilter> statFilters,
			ObjectProvider<ConfigFilter> configFilters, ObjectProvider<EncodingConvertFilter> encodingConvertFilters,
			ObjectProvider<Slf4jLogFilter> slf4jLogFilters, ObjectProvider<Log4jFilter> log4jFilters,
			ObjectProvider<Log4j2Filter> log4j2Filters, ObjectProvider<CommonsLogFilter> commonsLogFilters,
			ObjectProvider<WallFilter> wallFilters) {
		dataSource.getProxyFilters().clear();
		dataSource.getProxyFilters().addAll(statFilters.stream().collect(Collectors.toList()));
		dataSource.getProxyFilters().addAll(configFilters.stream().collect(Collectors.toList()));
		dataSource.getProxyFilters().addAll(encodingConvertFilters.stream().collect(Collectors.toList()));
		dataSource.getProxyFilters().addAll(slf4jLogFilters.stream().collect(Collectors.toList()));
		dataSource.getProxyFilters().addAll(log4jFilters.stream().collect(Collectors.toList()));
		dataSource.getProxyFilters().addAll(log4j2Filters.stream().collect(Collectors.toList()));
		dataSource.getProxyFilters().addAll(commonsLogFilters.stream().collect(Collectors.toList()));
		dataSource.getProxyFilters().addAll(wallFilters.stream().collect(Collectors.toList()));
	}
	
}
