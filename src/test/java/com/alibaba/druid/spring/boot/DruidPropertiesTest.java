package com.alibaba.druid.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alibaba.druid.spring.boot.ds.DruidDataSourceProperties;

/**
 * Tests for {@link DruidProperties}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class DruidPropertiesTest {

    private DruidProperties properties;

    @BeforeEach
    void setUp() {
        properties = new DruidProperties();
    }

    @Test
    @DisplayName("Default prefix is spring.datasource.druid")
    void defaultPrefix() {
        assertThat(DruidProperties.PREFIX).isEqualTo("spring.datasource.druid");
    }

    @Test
    @DisplayName("Default enabled is false")
    void defaultEnabled() {
        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Default routable is false")
    void defaultRoutable() {
        assertThat(properties.isRoutable()).isFalse();
    }

    @Test
    @DisplayName("Default slaves list is empty")
    void defaultSlaves() {
        assertThat(properties.getSlaves()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Getter and setter for enabled")
    void enabledGetterSetter() {
        properties.setEnabled(true);
        assertThat(properties.isEnabled()).isTrue();
        properties.setEnabled(false);
        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Getter and setter for routable")
    void routableGetterSetter() {
        properties.setRoutable(true);
        assertThat(properties.isRoutable()).isTrue();
        properties.setRoutable(false);
        assertThat(properties.isRoutable()).isFalse();
    }

    @Test
    @DisplayName("Getter and setter for slaves")
    void slavesGetterSetter() {
        DruidDataSourceProperties slave1 = new DruidDataSourceProperties();
        slave1.setName("slave1");
        DruidDataSourceProperties slave2 = new DruidDataSourceProperties();
        slave2.setName("slave2");
        List<DruidDataSourceProperties> slaves = List.of(slave1, slave2);
        properties.setSlaves(slaves);
        assertThat(properties.getSlaves()).hasSize(2);
        assertThat(properties.getSlaves().get(0).getName()).isEqualTo("slave1");
        assertThat(properties.getSlaves().get(1).getName()).isEqualTo("slave2");
    }

    @Test
    @DisplayName("Extends DruidDataSourceProperties")
    void extendsDruidDataSourceProperties() {
        assertThat(properties).isInstanceOf(DruidDataSourceProperties.class);
    }
}
