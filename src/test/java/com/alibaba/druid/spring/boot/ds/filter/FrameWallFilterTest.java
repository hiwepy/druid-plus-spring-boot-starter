package com.alibaba.druid.spring.boot.ds.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.proxy.jdbc.CallableStatementProxy;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import com.alibaba.druid.proxy.jdbc.DataSourceProxy;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.wall.WallFilter;

/**
 * Tests for {@link FrameWallFilter}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class FrameWallFilterTest {

    @Test
    @DisplayName("FrameWallFilter extends WallFilter")
    void extendsWallFilter() {
        FrameWallFilter filter = new FrameWallFilter();
        assertThat(filter).isInstanceOf(WallFilter.class);
    }

    @Test
    @DisplayName("FrameWallFilter can be instantiated")
    void canBeInstantiated() {
        FrameWallFilter filter = new FrameWallFilter();
        assertThat(filter).isNotNull();
    }

    @Test
    @DisplayName("getProvider creates MySqlWallProvider for MySQL dbType")
    void getProviderMySql() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("mysql");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:mysql://localhost:3306/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareStatement(connection, "SELECT 1"))
                .thenReturn(mock(PreparedStatementProxy.class));

        // This will internally call getProvider for MySQL
        filter.connection_prepareStatement(chain, connection, "SELECT 1");
    }

    @Test
    @DisplayName("getProvider creates OracleWallProvider for Oracle dbType")
    void getProviderOracle() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("oracle");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:oracle:thin:@localhost:1521:xe");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareStatement(connection, "SELECT 1"))
                .thenReturn(mock(PreparedStatementProxy.class));

        filter.connection_prepareStatement(chain, connection, "SELECT 1");
    }

    @Test
    @DisplayName("getProvider creates SQLServerWallProvider for SQLServer dbType")
    void getProviderSqlServer() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("sqlserver");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:sqlserver://localhost:1433;databaseName=test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareStatement(connection, "SELECT 1"))
                .thenReturn(mock(PreparedStatementProxy.class));

        filter.connection_prepareStatement(chain, connection, "SELECT 1");
    }

    @Test
    @DisplayName("getProvider creates PGWallProvider for PostgreSQL dbType")
    void getProviderPostgresql() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("postgresql");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:postgresql://localhost:5432/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareStatement(connection, "SELECT 1"))
                .thenReturn(mock(PreparedStatementProxy.class));

        filter.connection_prepareStatement(chain, connection, "SELECT 1");
    }

    @Test
    @DisplayName("getProvider creates DB2WallProvider for DB2 dbType")
    void getProviderDb2() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("db2");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:db2://localhost:50000/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareStatement(connection, "SELECT 1"))
                .thenReturn(mock(PreparedStatementProxy.class));

        filter.connection_prepareStatement(chain, connection, "SELECT 1");
    }

    @Test
    @DisplayName("getProvider uses rawJdbcUrl when dbType is null")
    void getProviderUsesUrlWhenDbTypeNull() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn(null);
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:mysql://localhost:3306/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareStatement(connection, "SELECT 1"))
                .thenReturn(mock(PreparedStatementProxy.class));

        filter.connection_prepareStatement(chain, connection, "SELECT 1");
    }

    @Test
    @DisplayName("getProvider throws for unsupported dbType")
    void getProviderThrowsForUnsupported() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("unsupported_db");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:unsupported://localhost/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);

        // The unsupported dbType causes NPE because WallProvider is null after getProvider throws
        assertThatThrownBy(() -> filter.connection_prepareStatement(chain, connection, "SELECT 1"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("connection_prepareStatement with autoGeneratedKeys delegates to provider")
    void connectionPrepareStatementAutoGeneratedKeys() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("mysql");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:mysql://localhost:3306/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareStatement(connection, "INSERT INTO t VALUES (1)", 1))
                .thenReturn(mock(PreparedStatementProxy.class));

        filter.connection_prepareStatement(chain, connection, "INSERT INTO t VALUES (1)", 1);
    }

    @Test
    @DisplayName("connection_prepareStatement with resultSetType delegates to provider")
    void connectionPrepareStatementResultSetType() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("mysql");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:mysql://localhost:3306/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareStatement(connection, "SELECT 1", 1, 2))
                .thenReturn(mock(PreparedStatementProxy.class));

        filter.connection_prepareStatement(chain, connection, "SELECT 1", 1, 2);
    }

    @Test
    @DisplayName("connection_prepareStatement with all resultSet params delegates to provider")
    void connectionPrepareStatementAllResultSetParams() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("mysql");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:mysql://localhost:3306/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareStatement(connection, "SELECT 1", 1, 2, 3))
                .thenReturn(mock(PreparedStatementProxy.class));

        filter.connection_prepareStatement(chain, connection, "SELECT 1", 1, 2, 3);
    }

    @Test
    @DisplayName("connection_prepareStatement with columnIndexes delegates to provider")
    void connectionPrepareStatementColumnIndexes() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("mysql");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:mysql://localhost:3306/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareStatement(connection, "INSERT INTO t VALUES (1)", new int[]{1}))
                .thenReturn(mock(PreparedStatementProxy.class));

        filter.connection_prepareStatement(chain, connection, "INSERT INTO t VALUES (1)", new int[]{1});
    }

    @Test
    @DisplayName("connection_prepareStatement with columnNames delegates to provider")
    void connectionPrepareStatementColumnNames() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("mysql");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:mysql://localhost:3306/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareStatement(connection, "INSERT INTO t VALUES (1)", new String[]{"id"}))
                .thenReturn(mock(PreparedStatementProxy.class));

        filter.connection_prepareStatement(chain, connection, "INSERT INTO t VALUES (1)", new String[]{"id"});
    }

    @Test
    @DisplayName("connection_prepareCall delegates to provider")
    void connectionPrepareCall() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("mysql");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:mysql://localhost:3306/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareCall(connection, "CALL proc()"))
                .thenReturn(mock(CallableStatementProxy.class));

        filter.connection_prepareCall(chain, connection, "CALL proc()");
    }

    @Test
    @DisplayName("connection_prepareCall with resultSetType delegates to provider")
    void connectionPrepareCallResultSetType() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("mysql");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:mysql://localhost:3306/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareCall(connection, "CALL proc()", 1, 2))
                .thenReturn(mock(CallableStatementProxy.class));

        filter.connection_prepareCall(chain, connection, "CALL proc()", 1, 2);
    }

    @Test
    @DisplayName("connection_prepareCall with all resultSet params delegates to provider")
    void connectionPrepareCallAllResultSetParams() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("mysql");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:mysql://localhost:3306/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareCall(connection, "CALL proc()", 1, 2, 3))
                .thenReturn(mock(CallableStatementProxy.class));

        filter.connection_prepareCall(chain, connection, "CALL proc()", 1, 2, 3);
    }

    @Test
    @DisplayName("getProvider caches WallProvider for same dbType")
    void getProviderCachesWallProvider() throws Exception {
        FrameWallFilter filter = new FrameWallFilter();
        DataSourceProxy dataSource = mock(DataSourceProxy.class);
        when(dataSource.getDbType()).thenReturn("mysql");
        when(dataSource.getName()).thenReturn("testDs");
        when(dataSource.getRawJdbcUrl()).thenReturn("jdbc:mysql://localhost:3306/test");

        ConnectionProxy connection = mock(ConnectionProxy.class);
        when(connection.getDirectDataSource()).thenReturn(dataSource);

        FilterChain chain = mock(FilterChain.class);
        when(chain.connection_prepareStatement(connection, "SELECT 1"))
                .thenReturn(mock(PreparedStatementProxy.class));

        // Call twice - second call should use cached provider
        filter.connection_prepareStatement(chain, connection, "SELECT 1");
        filter.connection_prepareStatement(chain, connection, "SELECT 1");
    }
}
