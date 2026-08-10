package com.alibaba.druid.spring.boot;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.util.CollectionUtils;

import com.alibaba.druid.filter.config.ConfigFilter;
import com.alibaba.druid.filter.encoding.EncodingConvertFilter;
import com.alibaba.druid.filter.logging.CommonsLogFilter;
import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.filter.logging.Log4jFilter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidFilterConfiguration;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidSpringAopConfiguration;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidStatViewServletConfiguration;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidWebStatFilterConfiguration;
import com.alibaba.druid.spring.boot.ds.DruidDataSourceProperties;
import com.alibaba.druid.spring.boot.ds.DynamicRoutingDataSource;
import com.alibaba.druid.spring.boot.ds.filter.FrameStatFilter;
import com.alibaba.druid.spring.boot.ds.filter.FrameWallFilter;
import com.alibaba.druid.spring.boot.util.DruidDataSourceUtils;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;

/**
 * DruidAutoConfiguration配置类，代替 DruidDataSourceAutoConfigure初始化Druid数据源，支持动态数据源
 * 记得在启动类添加@EnableAutoConfiguration(exclude={DruidDataSourceAutoConfigure.class})
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
@Configuration
@ConditionalOnClass(com.alibaba.druid.pool.DruidDataSource.class)
@AutoConfigureBefore(name = {
	"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
	"com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
})
@ConditionalOnProperty(prefix = DruidProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({DruidProperties.class, DruidStatProperties.class, DataSourceProperties.class})
@Import({DruidSpringAopConfiguration.class,
    DruidStatViewServletConfiguration.class,
    DruidWebStatFilterConfiguration.class,
    DruidFilterConfiguration.class})
public class DruidAutoConfiguration {

    private static final String FILTER_STAT_PREFIX = "spring.datasource.druid.filter.stat";
    private static final String FILTER_WALL_PREFIX = "spring.datasource.druid.filter.wall";
    
	/**
	 * 自定义Druid防火墙过滤器Bean
	 * @param wallConfig 防火墙过滤器配置Bean
	 * @return WallFilter
	 * @see com.alibaba.druid.spring.boot.autoconfigure.stat.DruidFilterConfiguration#wallFilter
	 */
	@Bean("wallFilter")
    @ConfigurationProperties(FILTER_WALL_PREFIX)
    @ConditionalOnProperty(prefix = FILTER_WALL_PREFIX, name = "enabled")
	@Primary
    public WallFilter wallFilter(@Qualifier("wallConfig") WallConfig wallConfig) {
        WallFilter filter = new FrameWallFilter();
        filter.setConfig(wallConfig);
        return filter;
    }

	/**
	 * 自定义Druid统计监控过滤器Bean
	 * @return StatFilter
	 * @see com.alibaba.druid.spring.boot.autoconfigure.stat.DruidFilterConfiguration#statFilter
	 */
	@Bean("statFilter")
	@ConfigurationProperties(FILTER_STAT_PREFIX)
    @ConditionalOnProperty(prefix = FILTER_STAT_PREFIX, name = "enabled", matchIfMissing = true)
	@Primary
	public StatFilter statFilter() {
		return new FrameStatFilter();
	}
   
	/*
	 * @Primary 该注解表示在同一个接口有多个实现类可以注入的时候，默认选择哪一个，而不是让@autowire注解报错
	 * @Qualifier 根据名称进行注入，通常是在具有相同的多个类型的实例的一个注入（例如有多个DataSource类型的实例）
	 */
	@Bean
	@Primary
	public DataSource dataSource(
			DataSourceProperties basicProperties, 
			DruidProperties druidProperties,
			ObjectProvider<StatFilter> statFilters,
			ObjectProvider<ConfigFilter> configFilters, 
			ObjectProvider<EncodingConvertFilter> encodingConvertFilters,
			ObjectProvider<Slf4jLogFilter> slf4jLogFilters,
			ObjectProvider<Log4jFilter> log4jFilters,
			ObjectProvider<Log4j2Filter> log4j2Filters,
			ObjectProvider<CommonsLogFilter> commonsLogFilters,
			ObjectProvider<WallFilter> wallFilters) {
		
		// 动态数据源
		if(druidProperties.isRoutable()) {

			Map<Object, Object> targetDataSources = new HashMap<Object, Object>();
			// 基于配置文件的动态数据源信息
			if (!CollectionUtils.isEmpty(druidProperties.getSlaves())) {
				for (DruidDataSourceProperties slaveProperties : druidProperties.getSlaves()) {
			        // 动态创建Druid数据源
			        DruidDataSource slaveDataSource = DruidDataSourceUtils.createDataSource(slaveProperties.configureProperties(basicProperties)) ;
			        // 配置过滤器
			        DruidDataSourceUtils.configureFilters(slaveDataSource, statFilters, configFilters, encodingConvertFilters, slf4jLogFilters, log4jFilters, log4j2Filters, commonsLogFilters, wallFilters);
					// 加入数据源
			        targetDataSources.put(slaveProperties.getName(), slaveDataSource);
				}
			}
			// 动态数据源支持
			DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
			// 该方法是AbstractRoutingDataSource的方法
			dataSource.setTargetDataSources(targetDataSources);

			// 默认的数据源
			DruidDataSource masterDataSource = DruidDataSourceUtils.createDataSource(druidProperties.configureProperties(basicProperties)) ; 
			// 配置过滤器
			DruidDataSourceUtils.configureFilters(masterDataSource, statFilters, configFilters, encodingConvertFilters, slf4jLogFilters, log4jFilters, log4j2Filters, commonsLogFilters, wallFilters);
			dataSource.setDefaultTargetDataSource(masterDataSource);
			
			return dataSource;
			
		}
		
		return DruidDataSourceBuilder.create().build();
	}

}
