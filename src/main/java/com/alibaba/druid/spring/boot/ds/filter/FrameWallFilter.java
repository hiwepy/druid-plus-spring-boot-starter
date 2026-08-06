/*
 * Copyright (c) 2018, hiwepy (https://github.com/easy-4-java).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.alibaba.druid.spring.boot.ds.filter;


import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.druid.DbType;
import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.proxy.jdbc.CallableStatementProxy;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import com.alibaba.druid.proxy.jdbc.DataSourceProxy;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.alibaba.druid.wall.WallProvider;
import com.alibaba.druid.wall.spi.DB2WallProvider;
import com.alibaba.druid.wall.spi.MySqlWallProvider;
import com.alibaba.druid.wall.spi.OracleWallProvider;
import com.alibaba.druid.wall.spi.PGWallProvider;
import com.alibaba.druid.wall.spi.SQLServerWallProvider;

/**
 * https://segmentfault.com/a/1190000014590536?utm_source=tag-newest
 * 自定义Druid防火墙过滤器
 * <p> 使用多类型数据源时，因共用WallProvider解析器，导致判断数据源类型出错 </p>
 * @author BBF
 * @see com.alibaba.druid.wall.WallFilter
 */
public class FrameWallFilter extends WallFilter {

  /**
   * 用线程安全的ConcurrentHashMap存储WallProvider对象
   */
  private final Map<DbType, WallProvider> providerMap = new ConcurrentHashMap<>(8);

	/**
	 * 获取WallProvider
	 * 
	 * @param dataSource
	 *            数据源
	 * @return WallProvider
	 */
	private WallProvider getProvider(DataSourceProxy dataSource) {
		String dbType;
		if (dataSource.getDbType() != null) {
			dbType = dataSource.getDbType();
		} else {
			dbType = JdbcUtils.getDbType(dataSource.getRawJdbcUrl(), "");
		}
		WallProvider provider;
		if (JdbcUtils.MYSQL.equals(dbType) || JdbcUtils.MARIADB.equals(dbType) || JdbcUtils.H2.equals(dbType)) {
			provider = providerMap.get(JdbcUtils.MYSQL);
			if (provider == null) {
				provider = new MySqlWallProvider(new WallConfig(MySqlWallProvider.DEFAULT_CONFIG_DIR));
				provider.setName(dataSource.getName());
				providerMap.put(JdbcUtils.MYSQL, provider);
			}
		} else if (JdbcUtils.ORACLE.equals(dbType) || JdbcUtils.ALI_ORACLE.equals(dbType)) {
			provider = providerMap.get(JdbcUtils.ORACLE);
			if (provider == null) {
				provider = new OracleWallProvider(new WallConfig(OracleWallProvider.DEFAULT_CONFIG_DIR));
				provider.setName(dataSource.getName());
				providerMap.put(JdbcUtils.ORACLE, provider);
			}
		} else if (JdbcUtils.SQL_SERVER.equals(dbType) || JdbcUtils.JTDS.equals(dbType)) {
			provider = providerMap.get(JdbcUtils.SQL_SERVER);
			if (provider == null) {
				provider = new SQLServerWallProvider(new WallConfig(SQLServerWallProvider.DEFAULT_CONFIG_DIR));
				provider.setName(dataSource.getName());
				providerMap.put(JdbcUtils.SQL_SERVER, provider);
			}
		} else if (JdbcUtils.POSTGRESQL.equals(dbType) || JdbcUtils.ENTERPRISEDB.equals(dbType)) {
			provider = providerMap.get(JdbcUtils.POSTGRESQL);
			if (provider == null) {
				provider = new PGWallProvider(new WallConfig(PGWallProvider.DEFAULT_CONFIG_DIR));
				provider.setName(dataSource.getName());
				providerMap.put(JdbcUtils.POSTGRESQL, provider);
			}
		} else if (JdbcUtils.DB2.equals(dbType)) {
			provider = providerMap.get(JdbcUtils.DB2);
			if (provider == null) {
				provider = new DB2WallProvider(new WallConfig(DB2WallProvider.DEFAULT_CONFIG_DIR));
				provider.setName(dataSource.getName());
				providerMap.put(JdbcUtils.DB2, provider);
			}
		} else {
			throw new IllegalStateException("dbType not support : " + dbType);
		}
		return provider;
	}

	/**
	 * 利用反射来更新父类私有变量provider
	 * @param connection ConnectionProxy
	 */
	private void setProvider(ConnectionProxy connection) {
		for (Class<?> cls = this.getClass(); cls != Object.class; cls = cls.getSuperclass()) {
			try {
				Field field = cls.getDeclaredField("provider");
				field.setAccessible(true);
				field.set(this, getProvider(connection.getDirectDataSource()));
			} catch (Exception e) {
				// Field不在当前类定义,继续向上转型
			}
		}
	}

	@Override
	public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection, String sql)
			throws SQLException {
		this.setProvider(connection);
		return super.connection_prepareStatement(chain, connection, sql);
	}

	@Override
	public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection, String sql,
			int autoGeneratedKeys) throws SQLException {
		this.setProvider(connection);
		return super.connection_prepareStatement(chain, connection, sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection, String sql,
			int resultSetType, int resultSetConcurrency) throws SQLException {
		this.setProvider(connection);
		return super.connection_prepareStatement(chain, connection, sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection, String sql,
			int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		this.setProvider(connection);
		return super.connection_prepareStatement(chain, connection, sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	@Override
	public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection, String sql,
			int[] columnIndexes) throws SQLException {
		this.setProvider(connection);
		return super.connection_prepareStatement(chain, connection, sql, columnIndexes);
	}

	@Override
	public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection, String sql,
			String[] columnNames) throws SQLException {
		this.setProvider(connection);
		return super.connection_prepareStatement(chain, connection, sql, columnNames);
	}

	@Override
	public CallableStatementProxy connection_prepareCall(FilterChain chain, ConnectionProxy connection, String sql)
			throws SQLException {
		this.setProvider(connection);
		return super.connection_prepareCall(chain, connection, sql);
	}

	@Override
	public CallableStatementProxy connection_prepareCall(FilterChain chain, ConnectionProxy connection, String sql,
			int resultSetType, int resultSetConcurrency) throws SQLException {
		this.setProvider(connection);
		return super.connection_prepareCall(chain, connection, sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatementProxy connection_prepareCall(FilterChain chain, ConnectionProxy connection, String sql,
			int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		this.setProvider(connection);
		return super.connection_prepareCall(chain, connection, sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

}