package com.alibaba.druid.spring.boot;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.alibaba.druid.spring.boot.ds.DruidDataSourceProperties;

@Getter
@Setter
@ConfigurationProperties(DruidProperties.PREFIX)
/**
 * <p>Auto-configuration for DruidProperties.</p>
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class DruidProperties extends DruidDataSourceProperties {

	public static final String PREFIX = "spring.datasource.druid";
	
	/**
	 * Enable Druid.
	 */
	private boolean enabled = false;
	/**
	 * Enable Dynamic Routing.
	 */
	private boolean routable = false;
	/** 
	 * Datasource slaves 
	 */
	private List<DruidDataSourceProperties> slaves = new ArrayList<DruidDataSourceProperties>();

	
}