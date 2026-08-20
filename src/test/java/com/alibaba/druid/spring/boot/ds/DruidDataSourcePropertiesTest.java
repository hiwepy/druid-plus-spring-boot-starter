package com.alibaba.druid.spring.boot.ds;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

/**
 * Tests for {@link DruidDataSourceProperties}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class DruidDataSourcePropertiesTest {

    private DruidDataSourceProperties properties;

    @BeforeEach
    void setUp() {
        properties = new DruidDataSourceProperties();
    }

    @Test
    @DisplayName("Default values are set correctly")
    void defaultValues() {
        assertThat(properties.getDriverClassName()).isNull();
        assertThat(properties.getName()).isNull();
        assertThat(properties.getUrl()).isNull();
        assertThat(properties.getUsername()).isNull();
        assertThat(properties.getPassword()).isNull();
        assertThat(properties.getDbType()).isNull();
        assertThat(properties.isAccessToUnderlyingConnectionAllowed()).isTrue();
        assertThat(properties.isAsyncCloseConnectionEnable()).isFalse();
        assertThat(properties.isAsyncInit()).isFalse();
        assertThat(properties.isCheckExecuteTime()).isFalse();
        assertThat(properties.isClearFiltersEnable()).isTrue();
        assertThat(properties.isDefaultAutoCommit()).isTrue();
        assertThat(properties.isDefaultReadOnly()).isFalse();
        assertThat(properties.getDefaultTransactionIsolation()).isNull();
        assertThat(properties.getDefaultCatalog()).isNull();
        assertThat(properties.isDupCloseLogEnable()).isFalse();
        assertThat(properties.isFailFast()).isFalse();
        assertThat(properties.getConnectionInitSqls()).isNull();
        assertThat(properties.isInitExceptionThrow()).isTrue();
        assertThat(properties.isInitGlobalVariants()).isFalse();
        assertThat(properties.isInitVariants()).isFalse();
        assertThat(properties.isKeepAlive()).isFalse();
        assertThat(properties.isKillWhenSocketReadTimeout()).isFalse();
        assertThat(properties.isLogAbandoned()).isFalse();
        assertThat(properties.isLogDifferentThread()).isTrue();
        assertThat(properties.getLoginTimeout()).isEqualTo(0);
        assertThat(properties.getMaxWaitThreadCount()).isEqualTo(-1);
        assertThat(properties.getNotFullTimeoutRetryCount()).isEqualTo(0);
        assertThat(properties.getMaxOpenPreparedStatements()).isEqualTo(-1);
        assertThat(properties.isRemoveAbandoned()).isFalse();
        assertThat(properties.isResetStatEnable()).isTrue();
        assertThat(properties.getConnectionErrorRetryAttempts()).isEqualTo(1);
        assertThat(properties.isBreakAfterAcquireFailure()).isFalse();
        assertThat(properties.getConnectProperties()).isNull();
        assertThat(properties.getMaxCreateTaskCount()).isEqualTo(3);
        assertThat(properties.getQueryTimeout()).isEqualTo(0);
        assertThat(properties.isPoolPreparedStatements()).isFalse();
        assertThat(properties.getStatSqlMaxSize()).isNull();
        assertThat(properties.isSharePreparedStatements()).isFalse();
        assertThat(properties.isTestOnBorrow()).isFalse();
        assertThat(properties.isTestOnReturn()).isFalse();
        assertThat(properties.getTransactionThresholdMillis()).isEqualTo(0L);
        assertThat(properties.isUseUnfairLock()).isFalse();
        assertThat(properties.isUseLocalSessionState()).isTrue();
        assertThat(properties.isUseGlobalDataSourceStat()).isFalse();
        assertThat(properties.getValidationQuery()).isEqualTo("SELECT 1");
        assertThat(properties.getValidationQueryTimeout()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Default connectionProperties has druid stat settings")
    void defaultConnectionProperties() {
        Properties props = properties.getConnectionProperties();
        assertThat(props).isNotNull();
        assertThat(props.getProperty("druid.stat.mergeSql")).isEqualTo("true");
        assertThat(props.getProperty("druid.stat.slowSqlMillis")).isEqualTo("5000");
    }

    @Test
    @DisplayName("Default filters is mergeStat,wall,slf4j")
    void defaultFilters() {
        assertThat(properties.getFilters()).isEqualTo("mergeStat,wall,slf4j");
    }

    @Test
    @DisplayName("Getter and setter for name")
    void nameGetterSetter() {
        properties.setName("testName");
        assertThat(properties.getName()).isEqualTo("testName");
    }

    @Test
    @DisplayName("Getter and setter for url")
    void urlGetterSetter() {
        properties.setUrl("jdbc:mysql://localhost:3306/test");
        assertThat(properties.getUrl()).isEqualTo("jdbc:mysql://localhost:3306/test");
    }

    @Test
    @DisplayName("Getter and setter for username")
    void usernameGetterSetter() {
        properties.setUsername("user");
        assertThat(properties.getUsername()).isEqualTo("user");
    }

    @Test
    @DisplayName("Getter and setter for password")
    void passwordGetterSetter() {
        properties.setPassword("pass");
        assertThat(properties.getPassword()).isEqualTo("pass");
    }

    @Test
    @DisplayName("Getter and setter for driverClassName")
    void driverClassNameGetterSetter() {
        properties.setDriverClassName("com.mysql.cj.jdbc.Driver");
        assertThat(properties.getDriverClassName()).isEqualTo("com.mysql.cj.jdbc.Driver");
    }

    @Test
    @DisplayName("toProperties returns correct druid properties")
    void toProperties() {
        properties.setName("myDs");
        properties.setDriverClassName("com.mysql.cj.jdbc.Driver");
        properties.setUrl("jdbc:mysql://localhost:3306/test");
        properties.setUsername("user");
        properties.setPassword("pass");
        properties.setStatSqlMaxSize(100);

        Properties result = properties.toProperties();
        assertThat(result).isNotNull();
        assertThat(result.getProperty("druid.name")).isEqualTo("myDs");
        assertThat(result.getProperty("druid.driverClassName")).isEqualTo("com.mysql.cj.jdbc.Driver");
        assertThat(result.getProperty("druid.url")).isEqualTo("jdbc:mysql://localhost:3306/test");
        assertThat(result.getProperty("druid.username")).isEqualTo("user");
        assertThat(result.getProperty("druid.password")).isEqualTo("pass");
        assertThat(result.getProperty("druid.stat.sql.MaxSize")).isEqualTo("100");
    }

    @Test
    @DisplayName("toProperties skips null values")
    void toPropertiesSkipsNull() {
        Properties result = properties.toProperties();
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("configureProperties copies from DataSourceProperties when own values are null")
    void configurePropertiesCopiesFromBasic() {
        DataSourceProperties basic = new DataSourceProperties();
        basic.setName("basicName");
        basic.setUsername("basicUser");
        basic.setPassword("basicPass");
        basic.setUrl("jdbc:h2:mem:basic");
        basic.setDriverClassName("org.h2.Driver");

        DruidDataSourceProperties result = properties.configureProperties(basic);

        assertThat(result).isSameAs(properties);
        assertThat(properties.getName()).isEqualTo("basicName");
        assertThat(properties.getUsername()).isEqualTo("basicUser");
        assertThat(properties.getPassword()).isEqualTo("basicPass");
        assertThat(properties.getUrl()).isEqualTo("jdbc:h2:mem:basic");
        assertThat(properties.getDriverClassName()).isEqualTo("org.h2.Driver");
    }

    @Test
    @DisplayName("configureProperties does not override existing values")
    void configurePropertiesDoesNotOverride() {
        properties.setName("existingName");
        properties.setUsername("existingUser");
        properties.setPassword("existingPass");
        properties.setUrl("jdbc:mysql://localhost:3306/existing");
        properties.setDriverClassName("com.mysql.cj.jdbc.Driver");

        DataSourceProperties basic = new DataSourceProperties();
        basic.setName("basicName");
        basic.setUsername("basicUser");
        basic.setPassword("basicPass");
        basic.setUrl("jdbc:mysql://localhost:3306/basic");
        basic.setDriverClassName("com.mysql.cj.jdbc.Driver");

        properties.configureProperties(basic);

        assertThat(properties.getName()).isEqualTo("existingName");
        assertThat(properties.getUsername()).isEqualTo("existingUser");
        assertThat(properties.getPassword()).isEqualTo("existingPass");
        assertThat(properties.getUrl()).isEqualTo("jdbc:mysql://localhost:3306/existing");
    }

    @Test
    @DisplayName("Getter and setter for maxActive")
    void maxActiveGetterSetter() {
        properties.setMaxActive(50);
        assertThat(properties.getMaxActive()).isEqualTo(50);
    }

    @Test
    @DisplayName("Getter and setter for minIdle")
    void minIdleGetterSetter() {
        properties.setMinIdle(5);
        assertThat(properties.getMinIdle()).isEqualTo(5);
    }

    @Test
    @DisplayName("Getter and setter for initialSize")
    void initialSizeGetterSetter() {
        properties.setInitialSize(10);
        assertThat(properties.getInitialSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Getter and setter for filters")
    void filtersGetterSetter() {
        properties.setFilters("stat,wall");
        assertThat(properties.getFilters()).isEqualTo("stat,wall");
    }

    @Test
    @DisplayName("Getter and setter for connectionInitSqls")
    void connectionInitSqlsGetterSetter() {
        List<String> sqls = List.of("SET NAMES utf8mb4", "SET time_zone='+08:00'");
        properties.setConnectionInitSqls(sqls);
        assertThat(properties.getConnectionInitSqls()).hasSize(2);
    }

    @Test
    @DisplayName("Getter and setter for keepAlive")
    void keepAliveGetterSetter() {
        properties.setKeepAlive(true);
        assertThat(properties.isKeepAlive()).isTrue();
    }
}
