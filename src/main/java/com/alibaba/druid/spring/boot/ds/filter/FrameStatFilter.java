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

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.stat.JdbcSqlStat;

/**
 * <p>多数据源 StatFilter 占位扩展。</p>
 * <p>目前与 Druid 原生 {@link StatFilter} 行为一致；如需在多数据源环境下
 * 按真实连接类型独立隔离 SQL 统计与慢 SQL 明细，请参考
 * {@link FrameWallFilter} 的 dbType 路由思路进行扩展。</p>
 * @author BBF
 * @see com.alibaba.druid.filter.stat.StatFilter#createSqlStat(StatementProxy, String)
 * @since 1.0.0
 */
public class FrameStatFilter extends StatFilter {
	
	@Override
	/**
	 * <p>Create sql stat.</p>
	 * @param statement
	 * @param sql
	 * @return the result
	 */
	public JdbcSqlStat createSqlStat(StatementProxy statement, String sql) {
		return super.createSqlStat(statement, sql);
	}
	
}