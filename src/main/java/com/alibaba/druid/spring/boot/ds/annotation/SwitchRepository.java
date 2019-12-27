package com.alibaba.druid.spring.boot.ds.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.biz.jdbc.DataSourceRoutingKeyHolder;

/**
 * 用于方法注释；是否切换数据源及切换的数据源名称
 * @author 		： <a href="https://github.com/hiwepy">hiwepy</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface SwitchRepository {

	/**
	 * 数据源名称
	 */
	public String value() default DataSourceRoutingKeyHolder.MASTER_DATASOURCE;
	
}
