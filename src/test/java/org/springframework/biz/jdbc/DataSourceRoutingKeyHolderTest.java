package org.springframework.biz.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DataSourceRoutingKeyHolder unit tests (max coverage to hit JaCoCo 90%+)")
class DataSourceRoutingKeyHolderTest {

    @BeforeEach
    void setUp() {
        DataSourceRoutingKeyHolder.clearDataSourceKey();
        DataSourceRoutingKeyHolder.dataSourceKeys = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        DataSourceRoutingKeyHolder.clearDataSourceKey();
        DataSourceRoutingKeyHolder.dataSourceKeys = new ArrayList<>();
    }

    @Test
    @DisplayName("MASTER_DATASOURCE constant must be defaultDataSource")
    void masterConstant() {
        assertThat(DataSourceRoutingKeyHolder.MASTER_DATASOURCE).isEqualTo("defaultDataSource");
    }

    @Test
    @DisplayName("getDataSourceKey returns MASTER default when nothing set")
    void initialGetIsMaster() {
        assertThat(DataSourceRoutingKeyHolder.getDataSourceKey()).isEqualTo(DataSourceRoutingKeyHolder.MASTER_DATASOURCE);
    }

    @Test
    @DisplayName("setDataSourceKey then getDataSourceKey returns same value")
    void setGetRoundTrip() {
        DataSourceRoutingKeyHolder.setDataSourceKey("read1");
        assertThat(DataSourceRoutingKeyHolder.getDataSourceKey()).isEqualTo("read1");
    }

    @Test
    @DisplayName("setDataSourceKey(null) falls back to master")
    void setNullFallsBack() {
        DataSourceRoutingKeyHolder.setDataSourceKey(null);
        assertThat(DataSourceRoutingKeyHolder.getDataSourceKey()).isEqualTo(DataSourceRoutingKeyHolder.MASTER_DATASOURCE);
    }

    @Test
    @DisplayName("setDataSourceKey(blank) falls back to master")
    void setBlankFallsBack() {
        DataSourceRoutingKeyHolder.setDataSourceKey("   ");
        assertThat(DataSourceRoutingKeyHolder.getDataSourceKey()).isEqualTo(DataSourceRoutingKeyHolder.MASTER_DATASOURCE);
    }

    @Test
    @DisplayName("clearDataSourceKey resets context holder, get returns master again")
    void clearWorks() {
        DataSourceRoutingKeyHolder.setDataSourceKey("other");
        DataSourceRoutingKeyHolder.clearDataSourceKey();
        assertThat(DataSourceRoutingKeyHolder.getDataSourceKey()).isEqualTo(DataSourceRoutingKeyHolder.MASTER_DATASOURCE);
    }

    @Test
    @DisplayName("useSlaveDataSource picks a non-master key if available")
    void useSlavePicksSlave() {
        DataSourceRoutingKeyHolder.dataSourceKeys.addAll(Arrays.asList(
                DataSourceRoutingKeyHolder.MASTER_DATASOURCE,
                "read1",
                "read2"
        ));
        DataSourceRoutingKeyHolder.useSlaveDataSource();
        String key = DataSourceRoutingKeyHolder.getDataSourceKey();
        assertThat(key).isIn("read1", "read2");
    }

    @Test
    @DisplayName("useSlaveDataSource with empty keys falls back to master")
    void useSlaveWithEmptyKeysIsMaster() {
        DataSourceRoutingKeyHolder.dataSourceKeys = Collections.emptyList();
        DataSourceRoutingKeyHolder.useSlaveDataSource();
        assertThat(DataSourceRoutingKeyHolder.getDataSourceKey()).isEqualTo(DataSourceRoutingKeyHolder.MASTER_DATASOURCE);
    }

    @Test
    @DisplayName("useSlaveDataSource with only master available falls back to master")
    void useSlaveWithOnlyMasterIsMaster() {
        DataSourceRoutingKeyHolder.dataSourceKeys.add(DataSourceRoutingKeyHolder.MASTER_DATASOURCE);
        DataSourceRoutingKeyHolder.useSlaveDataSource();
        assertThat(DataSourceRoutingKeyHolder.getDataSourceKey()).isEqualTo(DataSourceRoutingKeyHolder.MASTER_DATASOURCE);
    }

    @Test
    @DisplayName("containDataSourceKey finds key present in list")
    void containKeyWorks() {
        DataSourceRoutingKeyHolder.dataSourceKeys.add("alpha");
        DataSourceRoutingKeyHolder.dataSourceKeys.add(42);  // mixed types stored as Object
        assertThat(DataSourceRoutingKeyHolder.containDataSourceKey("alpha")).isTrue();
        assertThat(DataSourceRoutingKeyHolder.containDataSourceKey("42")).isTrue();
        assertThat(DataSourceRoutingKeyHolder.containDataSourceKey("missing")).isFalse();
        assertThat(DataSourceRoutingKeyHolder.containDataSourceKey(null)).isFalse();
    }

    @Test
    @DisplayName("Constructor invocation to cover <init> path (already public for instantiation tests like CGLIB)")
    void instantiationDoesNotFail() {
        DataSourceRoutingKeyHolder holder = new DataSourceRoutingKeyHolder();
        assertThat(holder).isNotNull();
    }
}
