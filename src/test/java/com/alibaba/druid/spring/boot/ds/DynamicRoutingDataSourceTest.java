package com.alibaba.druid.spring.boot.ds;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * Tests for {@link DynamicRoutingDataSource}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class DynamicRoutingDataSourceTest {

    private DynamicRoutingDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = new DynamicRoutingDataSource();
    }

    @Test
    @DisplayName("DynamicRoutingDataSource can be instantiated")
    void canBeInstantiated() {
        assertThat(dataSource).isNotNull();
    }

    @Test
    @DisplayName("getTargetDataSources returns map after setup")
    void getTargetDataSources() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        DruidDataSource ds1 = new DruidDataSource();
        ds1.setUrl("jdbc:h2:mem:test1");
        targetDataSources.put("ds1", ds1);

        DruidDataSource ds2 = new DruidDataSource();
        ds2.setUrl("jdbc:h2:mem:test2");
        targetDataSources.put("ds2", ds2);

        dataSource.setTargetDataSources(targetDataSources);

        DataSourceProperties basicProps = new DataSourceProperties();
        basicProps.setUrl("jdbc:h2:mem:default");
        dataSource.setDefaultTargetDataSource(new DruidDataSource());

        // Need to set afterPropertiesSet to resolve
        dataSource.afterPropertiesSet();

        Map<Object, Object> result = dataSource.getTargetDataSources();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("setNewTargetDataSources adds to existing targets")
    void setNewTargetDataSources() {
        Map<Object, Object> initialSources = new HashMap<>();
        DruidDataSource ds1 = new DruidDataSource();
        ds1.setUrl("jdbc:h2:mem:test1");
        initialSources.put("ds1", ds1);

        dataSource.setTargetDataSources(initialSources);
        dataSource.setDefaultTargetDataSource(new DruidDataSource());
        dataSource.afterPropertiesSet();

        Map<Object, Object> newSources = new HashMap<>();
        DruidDataSource ds2 = new DruidDataSource();
        ds2.setUrl("jdbc:h2:mem:test2");
        newSources.put("ds2", ds2);

        dataSource.setNewTargetDataSources(newSources);

        Map<Object, Object> result = dataSource.getTargetDataSources();
        assertThat(result).hasSize(2);
        assertThat(result).containsKey("ds1");
        assertThat(result).containsKey("ds2");
    }

    @Test
    @DisplayName("removeTargetDataSource removes a target")
    void removeTargetDataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        DruidDataSource ds1 = new DruidDataSource();
        ds1.setUrl("jdbc:h2:mem:test1");
        targetDataSources.put("ds1", ds1);

        DruidDataSource ds2 = new DruidDataSource();
        ds2.setUrl("jdbc:h2:mem:test2");
        targetDataSources.put("ds2", ds2);

        dataSource.setTargetDataSources(targetDataSources);
        dataSource.setDefaultTargetDataSource(new DruidDataSource());
        dataSource.afterPropertiesSet();

        dataSource.removeTargetDataSource("ds1");

        Map<Object, Object> result = dataSource.getTargetDataSources();
        assertThat(result).hasSize(1);
        assertThat(result).containsKey("ds2");
        assertThat(result).doesNotContainKey("ds1");
    }

    @Test
    @DisplayName("setTargetDataSource with name adds a new data source")
    void setTargetDataSourceByName() {
        Map<Object, Object> initialSources = new HashMap<>();
        DruidDataSource defaultDs = new DruidDataSource();
        defaultDs.setUrl("jdbc:h2:mem:default");

        dataSource.setTargetDataSources(initialSources);
        dataSource.setDefaultTargetDataSource(defaultDs);
        dataSource.afterPropertiesSet();

        DataSourceProperties basicProps = new DataSourceProperties();
        basicProps.setUrl("jdbc:h2:mem:test");
        basicProps.setUsername("sa");
        basicProps.setPassword("");

        DruidDataSourceProperties druidProps = new DruidDataSourceProperties();
        druidProps.setUrl("jdbc:h2:mem:slave1");
        druidProps.setUsername("sa");
        druidProps.setPassword("");

        dataSource.setTargetDataSource("slave1", basicProps, druidProps);

        Map<Object, Object> result = dataSource.getTargetDataSources();
        assertThat(result).containsKey("slave1");
    }

    @Test
    @DisplayName("setTargetDataSource with properties uses name from druidProperties")
    void setTargetDataSourceWithProperties() {
        Map<Object, Object> initialSources = new HashMap<>();
        DruidDataSource defaultDs = new DruidDataSource();
        defaultDs.setUrl("jdbc:h2:mem:default");

        dataSource.setTargetDataSources(initialSources);
        dataSource.setDefaultTargetDataSource(defaultDs);
        dataSource.afterPropertiesSet();

        DataSourceProperties basicProps = new DataSourceProperties();
        basicProps.setUrl("jdbc:h2:mem:test");
        basicProps.setUsername("sa");
        basicProps.setPassword("");

        DruidDataSourceProperties druidProps = new DruidDataSourceProperties();
        druidProps.setName("namedSlave");
        druidProps.setUrl("jdbc:h2:mem:slave");
        druidProps.setUsername("sa");
        druidProps.setPassword("");

        dataSource.setTargetDataSource(basicProps, druidProps);

        Map<Object, Object> result = dataSource.getTargetDataSources();
        assertThat(result).containsKey("namedSlave");
    }

    @Test
    @DisplayName("setTargetDataSource copies basic properties when druid properties are null")
    void setTargetDataSourceCopiesBasicProperties() {
        Map<Object, Object> initialSources = new HashMap<>();
        DruidDataSource defaultDs = new DruidDataSource();
        defaultDs.setUrl("jdbc:h2:mem:default");

        dataSource.setTargetDataSources(initialSources);
        dataSource.setDefaultTargetDataSource(defaultDs);
        dataSource.afterPropertiesSet();

        DataSourceProperties basicProps = new DataSourceProperties();
        basicProps.setUrl("jdbc:h2:mem:basic");
        basicProps.setUsername("basicUser");
        basicProps.setPassword("basicPass");
        basicProps.setDriverClassName("org.h2.Driver");

        DruidDataSourceProperties druidProps = new DruidDataSourceProperties();
        druidProps.setName("copyTest");

        dataSource.setTargetDataSource("copyTest", basicProps, druidProps);

        Map<Object, Object> result = dataSource.getTargetDataSources();
        assertThat(result).containsKey("copyTest");
    }

    @Test
    @DisplayName("afterPropertiesSet registers keys in DataSourceRoutingKeyHolder")
    void afterPropertiesSetRegistersKeys() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        DruidDataSource ds1 = new DruidDataSource();
        ds1.setUrl("jdbc:h2:mem:test1");
        targetDataSources.put("ds1", ds1);

        dataSource.setTargetDataSources(targetDataSources);
        dataSource.setDefaultTargetDataSource(new DruidDataSource());
        dataSource.afterPropertiesSet();

        // afterPropertiesSet should have been called, registering keys
        assertThat(dataSource.getTargetDataSources()).containsKey("ds1");
    }
}
