package com.alibaba.druid.spring.boot;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

	@Primary
	@Bean
	@ConfigurationProperties("spring.datasource.druid.one")
	public DataSource dataSourceOne(){
	    return DruidDataSourceBuilder.create().build();
	}
	@Bean
	@ConfigurationProperties("spring.datasource.druid.two")
	public DataSource dataSourceTwo(){
	    return DruidDataSourceBuilder.create().build();
	}
	
	@Test
	public void contextLoads() {
	}

}
