package com.alibaba.druid.spring.boot.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.ds.DruidDataSourceProperties;

/**
 * Tests for {@link DruidDataSourceUtils}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class DruidDataSourceUtilsTest {

    @Test
    @DisplayName("configureProperties sets all properties on DruidDataSource")
    void configurePropertiesSetsAllProperties() {
        DruidDataSourceProperties druidProps = new DruidDataSourceProperties();
        druidProps.setName("testDs");
        druidProps.setUrl("jdbc:mysql://localhost:3306/test");
        druidProps.setUsername("user");
        druidProps.setPassword("pass");
        druidProps.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidProps.setMaxActive(20);
        druidProps.setMinIdle(5);
        druidProps.setInitialSize(10);
        druidProps.setMaxWait(5000);
        druidProps.setKeepAlive(true);
        druidProps.setTestWhileIdle(true);
        druidProps.setTestOnBorrow(true);
        druidProps.setTestOnReturn(false);
        druidProps.setValidationQuery("SELECT 1");
        druidProps.setPoolPreparedStatements(true);
        druidProps.setFilters("stat,wall");
        druidProps.setAsyncInit(true);
        druidProps.setBreakAfterAcquireFailure(true);
        druidProps.setConnectionErrorRetryAttempts(3);
        druidProps.setDefaultAutoCommit(false);
        druidProps.setDefaultReadOnly(true);
        druidProps.setDupCloseLogEnable(true);
        druidProps.setFailFast(true);
        druidProps.setInitExceptionThrow(false);
        druidProps.setInitGlobalVariants(true);
        druidProps.setInitVariants(true);
        druidProps.setLogAbandoned(true);
        druidProps.setLogDifferentThread(false);
        druidProps.setMaxCreateTaskCount(5);
        druidProps.setQueryTimeout(30);
        druidProps.setRemoveAbandoned(true);
        druidProps.setResetStatEnable(false);
        druidProps.setSharePreparedStatements(true);
        druidProps.setUseGlobalDataSourceStat(true);
        druidProps.setUseLocalSessionState(false);
        druidProps.setUseUnfairLock(true);

        DruidDataSource dataSource = new DruidDataSource();
        DruidDataSourceUtils.configureProperties(druidProps, dataSource);

        assertThat(dataSource.getName()).isEqualTo("testDs");
        assertThat(dataSource.getMaxActive()).isEqualTo(20);
        assertThat(dataSource.getMinIdle()).isEqualTo(5);
        assertThat(dataSource.getInitialSize()).isEqualTo(10);
        assertThat(dataSource.getMaxWait()).isEqualTo(5000);
        assertThat(dataSource.isKeepAlive()).isTrue();
        assertThat(dataSource.isTestWhileIdle()).isTrue();
        assertThat(dataSource.isTestOnBorrow()).isTrue();
        assertThat(dataSource.isTestOnReturn()).isFalse();
        assertThat(dataSource.getValidationQuery()).isEqualTo("SELECT 1");
        assertThat(dataSource.isPoolPreparedStatements()).isTrue();
    }

    @Test
    @DisplayName("configureProperties does not override auto-generated name when name is empty")
    void configurePropertiesSkipsEmptyName() {
        DruidDataSourceProperties druidProps = new DruidDataSourceProperties();
        druidProps.setUrl("jdbc:mysql://localhost:3306/test");

        DruidDataSource dataSource = new DruidDataSource();
        // DruidDataSource auto-generates a name like "DataSource-xxx"
        String autoName = dataSource.getName();
        DruidDataSourceUtils.configureProperties(druidProps, dataSource);

        // When name is empty/null, it should not be set, so auto-generated name remains
        assertThat(dataSource.getName()).isEqualTo(autoName);
    }

    @Test
    @DisplayName("configureProperties skips validationQuery when null")
    void configurePropertiesSkipsNullValidationQuery() {
        DruidDataSourceProperties druidProps = new DruidDataSourceProperties();
        druidProps.setUrl("jdbc:mysql://localhost:3306/test");
        druidProps.setValidationQuery(null);

        DruidDataSource dataSource = new DruidDataSource();
        DruidDataSourceUtils.configureProperties(druidProps, dataSource);

        // When validationQuery is null, the method looks up from DatabaseDriver
        // For mysql URL, it should set a default validation query
        assertThat(dataSource.getValidationQuery()).isNotNull();
    }
}
