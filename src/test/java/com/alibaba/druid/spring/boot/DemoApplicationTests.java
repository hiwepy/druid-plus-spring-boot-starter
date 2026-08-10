package com.alibaba.druid.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Basic smoke tests for the druid-plus-spring-boot-starter module.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class DemoApplicationTests {

    @Test
    @DisplayName("DruidProperties can be instantiated")
    void druidPropertiesCanBeInstantiated() {
        DruidProperties properties = new DruidProperties();
        assertThat(properties).isNotNull();
    }

    @Test
    @DisplayName("DruidProperties has correct prefix")
    void druidPropertiesHasCorrectPrefix() {
        assertThat(DruidProperties.PREFIX).isEqualTo("spring.datasource.druid");
    }
}
