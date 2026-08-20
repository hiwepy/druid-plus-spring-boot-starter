package com.alibaba.druid.spring.boot.ds;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.biz.jdbc.DataSourceRoutingKeyHolder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.ReflectionUtils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.util.DruidDataSourceUtils;


@SuppressWarnings("unchecked")
/**
 * <p>Auto-configuration for DynamicRoutingDataSource.</p>
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

	/**
     * 用于在维护数据源时保证不会被其他线程修改
     */
    private static final Lock lock = new ReentrantLock();
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected static Field targetDataSourcesField = ReflectionUtils.findField(AbstractRoutingDataSource.class,
			"targetDataSources");
	protected static Field resolvedDataSourcesField = ReflectionUtils.findField(AbstractRoutingDataSource.class,
			"resolvedDataSources");
	
	@Override
	/**
	 * <p>Determine current lookup key.</p>
	 * @return the result
	 */
	protected Object determineCurrentLookupKey() {
		 logger.info("Current DataSource is [{}]", DataSourceRoutingKeyHolder.getDataSourceKey());
		return DataSourceRoutingKeyHolder.getDataSourceKey();
	}
	
	/** @return return the target data sources. */
	public Map<Object, Object> getTargetDataSources() {
		targetDataSourcesField.setAccessible(true);
		Object targetDataSources = ReflectionUtils.getField(targetDataSourcesField, this);
		targetDataSourcesField.setAccessible(false);
		return (Map<Object, Object>) targetDataSources;
	}

	/** @return return the resolved data sources. */
	public Map<Object, DataSource> getResolvedDataSources() {
		resolvedDataSourcesField.setAccessible(true);
		Object resolvedDataSources = ReflectionUtils.getField(resolvedDataSourcesField, this);
		resolvedDataSourcesField.setAccessible(false);
        return (Map<Object, DataSource>) resolvedDataSources;
	}
	
	/** @param druidProperties set the target data source. */
	public void setTargetDataSource(String name, DataSourceProperties basicProperties, DruidDataSourceProperties druidProperties) {

		lock.lock();
		
		try {
			
			//if not found prefix 'spring.datasource.druid' jdbc properties ,'spring.datasource' prefix jdbc properties will be used.
	        if (druidProperties.getUsername() == null) {
	        	druidProperties.setUsername(basicProperties.determineUsername());
	        }
	        if (druidProperties.getPassword() == null) {
	        	druidProperties.setPassword(basicProperties.determinePassword());
	        }
	        if (druidProperties.getUrl() == null) {
	        	druidProperties.setUrl(basicProperties.determineUrl());
	        }
	        if(druidProperties.getDriverClassName() == null){
	        	druidProperties.setDriverClassName(basicProperties.determineDriverClassName());
	        }
			
			// 动态创建Druid数据源
			DruidDataSource targetDataSource = DruidDataSourceUtils.createDataSource(druidProperties);

			getTargetDataSources().put(name, targetDataSource);
			
			// reset resolvedDataSources
			this.afterPropertiesSet();
			
		} finally {
            lock.unlock();
        }
		
	}
	
	/** @param druidProperties set the target data source. */
	public void setTargetDataSource(DataSourceProperties properties, DruidDataSourceProperties druidProperties) {
		this.setTargetDataSource(druidProperties.getName(), properties, druidProperties);
	}

	/** @param targetDataSources set the new target data sources. */
	public void setNewTargetDataSources(Map<Object, Object> targetDataSources) {
		
		lock.lock();
		
		try {
			
			getTargetDataSources().putAll(targetDataSources);
			// reset resolvedDataSources
			this.afterPropertiesSet();
			
		} finally {
	        lock.unlock();
	    }
	}

	/**
	 * <p>Remove target data source.</p>
	 * @param name
	 */
	public void removeTargetDataSource(String name) {
		
		lock.lock();
		
		try {
			
			getTargetDataSources().remove(name);
			// reset resolvedDataSources
			this.afterPropertiesSet();
		
		} finally {
	        lock.unlock();
	    }
	}
	
	@Override
	/**
	 * <p>After properties set.</p>
	 */
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		getTargetDataSources().forEach((key, value) -> {
			Object lookupKey = resolveSpecifiedLookupKey(key);
			DataSourceRoutingKeyHolder.dataSourceKeys.add(lookupKey);
		});
	}
	
}
