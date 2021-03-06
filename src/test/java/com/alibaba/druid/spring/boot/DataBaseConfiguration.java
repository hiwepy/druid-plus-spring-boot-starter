package com.alibaba.druid.spring.boot;


import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.alibaba.druid.pool.DruidDataSource;  

@Configuration  
@EnableTransactionManagement  
public class DataBaseConfiguration implements EnvironmentAware {  


  private static Logger log = LoggerFactory.getLogger(DataBaseConfiguration.class);  
    
  @Override  
  public void setEnvironment(Environment env) {  
  }  

  @Bean(name="writeDataSource", destroyMethod = "close", initMethod="init")  
  @Primary  
  public DataSource writeDataSource() {  
      log.debug("Configruing Write DataSource");  
        
      DruidDataSource datasource = new DruidDataSource();  
      /*datasource.setUrl(propertyResolver.getProperty("url"));  
      datasource.setDriverClassName(propertyResolver.getProperty("driverClassName"));  
      datasource.setUsername(propertyResolver.getProperty("username"));  
      datasource.setPassword(propertyResolver.getProperty("password"));  
       */ 
      return datasource;  
  }  
    
  @Bean(name="readOneDataSource", destroyMethod = "close", initMethod="init")  
  public DataSource readOneDataSource() {  
      log.debug("Configruing Read One DataSource");  
        
      DruidDataSource datasource = new DruidDataSource();  
      /*datasource.setUrl(propertyResolver.getProperty("url"));  
      datasource.setDriverClassName(propertyResolver.getProperty("driverClassName"));  
      datasource.setUsername(propertyResolver.getProperty("username"));  
      datasource.setPassword(propertyResolver.getProperty("password"));  
        */
      return datasource;  
  }  
    
  @Bean(name="readTowDataSource", destroyMethod = "close", initMethod="init")  
  public DataSource readTowDataSource() {  
      log.debug("Configruing Read Two DataSource");  
        
      DruidDataSource datasource = new DruidDataSource();  
      /*datasource.setUrl(propertyResolver.getProperty("url"));  
      datasource.setDriverClassName(propertyResolver.getProperty("driverClassName"));  
      datasource.setUsername(propertyResolver.getProperty("username"));  
      datasource.setPassword(propertyResolver.getProperty("password"));  
        */
      return datasource;  
  }  
    
    
  @Bean(name="readDataSources")  
  public List<DataSource> readDataSources(){  
      List<DataSource> dataSources = new ArrayList<DataSource>();  
      dataSources.add(readOneDataSource());  
      dataSources.add(readTowDataSource());  
      return dataSources;  
  }  
    
}  