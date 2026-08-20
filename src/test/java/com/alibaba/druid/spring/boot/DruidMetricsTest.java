/*
 * Copyright (c) 2018-present, easy-4-java (https://github.com/easy-4-java).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.spring.boot;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.metrics.DruidMetrics;
import com.alibaba.druid.stat.JdbcDataSourceStat;
import com.alibaba.druid.stat.JdbcSqlStat;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DruidMetrics}.
 *
 * <p>Tests use a {@link SimpleMeterRegistry} to verify that the binder
 * publishes a comprehensive set of gauges for every configured
 * {@link DruidDataSource} and that those gauges carry the expected
 * {@code pool} tag.</p>
 *
 * <p>Because Druid's {@code getDataSourceStat()} only returns a non-null
 * value after the pool has been initialised (which would require a JDBC
 * driver), tests inject a fresh {@link JdbcDataSourceStat} reflectively
 * &mdash; the binder only reads from that object, so a default instance
 * is sufficient.</p>
 *
 * @since 3.0.0
 */
class DruidMetricsTest {

	private MeterRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new SimpleMeterRegistry();
	}

	@Test
	void shouldRegisterGaugesForEveryConfiguredDataSource() {
		DruidDataSource ds1 = injectStat(new DruidDataSource());
		DruidDataSource ds2 = injectStat(new DruidDataSource());

		Map<String, DruidDataSource> dataSources = new HashMap<>();
		dataSources.put("primary", ds1);
		dataSources.put("secondary", ds2);

		DruidMetrics metrics = new DruidMetrics(dataSources);
		metrics.bindTo(registry);

		List<String> poolTags = registry.getMeters().stream()
				.map(Meter::getId)
				.flatMap(id -> id.getTags().stream())
				.filter(tag -> "pool".equals(tag.getKey()))
				.map(Tag::getValue)
				.distinct()
				.collect(Collectors.toList());

		assertTrue(poolTags.contains("primary"),
				"gauges for the 'primary' data source must be registered");
		assertTrue(poolTags.contains("secondary"),
				"gauges for the 'secondary' data source must be registered");
	}

	@Test
	void shouldHandleEmptyDataSourceMapGracefully() {
		DruidMetrics metrics = new DruidMetrics(Collections.emptyMap());
		metrics.bindTo(registry);

		// No exceptions should be raised; the registry stays empty.
		assertEquals(0, registry.getMeters().size());
	}

	@Test
	void shouldPublishPoolConfigurationGauges() {
		DruidDataSource dataSource = injectStat(new DruidDataSource());
		dataSource.setMaxActive(20);
		dataSource.setMinIdle(3);

		Map<String, DruidDataSource> dataSources = Collections.singletonMap("primary", dataSource);
		DruidMetrics metrics = new DruidMetrics(dataSources);
		metrics.bindTo(registry);

		assertGaugeValue("druid.max.active", 20.0, registry);
		assertGaugeValue("druid.min.idle", 3.0, registry);
	}

	@Test
	void shouldAttachPoolTagToAllEmittedMeters() {
		DruidDataSource dataSource = injectStat(new DruidDataSource());
		Map<String, DruidDataSource> dataSources = Collections.singletonMap("primary", dataSource);

		DruidMetrics metrics = new DruidMetrics(dataSources);
		metrics.bindTo(registry);

		long metersWithoutPoolTag = registry.getMeters().stream()
				.filter(meter -> meter.getId().getTags().stream()
						.noneMatch(tag -> "pool".equals(tag.getKey())))
				.count();

		assertEquals(0L, metersWithoutPoolTag,
				"every emitted meter must be tagged with the 'pool' tag");
	}

	@Test
	void shouldRegisterAtLeastOneGaugeForAConfiguredDataSource() {
		DruidDataSource dataSource = injectStat(new DruidDataSource());
		Map<String, DruidDataSource> dataSources = Collections.singletonMap("primary", dataSource);

		DruidMetrics metrics = new DruidMetrics(dataSources);
		metrics.bindTo(registry);

		assertTrue(registry.getMeters().size() > 0,
				"binding a non-empty data-source map must register at least one meter");
	}

	@Test
	void shouldExposePrefixConstant() {
		// The constant is intentionally part of the public API so that
		// downstream dashboards can rely on it.
		assertEquals("druid", DruidMetrics.DRUID_METRIC_NAME_PREFIX);
	}

	@Test
	void shouldRegisterConnectionGauges() {
		DruidDataSource dataSource = injectStat(new DruidDataSource());
		Map<String, DruidDataSource> dataSources = Collections.singletonMap("primary", dataSource);

		DruidMetrics metrics = new DruidMetrics(dataSources);
		metrics.bindTo(registry);

		// Druid's JdbcConnectionStat exposes a connect count.  Look up the
		// matching meter to confirm it was bound to the registry.
		Gauge gauge = registry.find("druid.connections.connect.count").gauge();
		assertNotNull(gauge,
				"druid.connections.connect.count must be registered for the pool");
	}

	@Test
	void shouldRegisterStatementGauges() {
		DruidDataSource dataSource = injectStat(new DruidDataSource());
		Map<String, DruidDataSource> dataSources = Collections.singletonMap("primary", dataSource);

		DruidMetrics metrics = new DruidMetrics(dataSources);
		metrics.bindTo(registry);

		Gauge gauge = registry.find("druid.statement.create.count").gauge();
		assertNotNull(gauge,
				"druid.statement.create.count must be registered for the pool");
	}

	@Test
	void shouldRegisterResultSetGauges() {
		DruidDataSource dataSource = injectStat(new DruidDataSource());
		Map<String, DruidDataSource> dataSources = Collections.singletonMap("primary", dataSource);

		DruidMetrics metrics = new DruidMetrics(dataSources);
		metrics.bindTo(registry);

		Gauge gauge = registry.find("druid.resultset.open.count").gauge();
		assertNotNull(gauge,
				"druid.resultset.open.count must be registered for the pool");
	}

	@Test
	void shouldRegisterSqlSkipCountGaugeEvenWithoutAnyExecutedSql() {
		DruidDataSource dataSource = injectStat(new DruidDataSource());
		Map<String, DruidDataSource> dataSources = Collections.singletonMap("primary", dataSource);

		DruidMetrics metrics = new DruidMetrics(dataSources);
		metrics.bindTo(registry);

		// The skip-sql gauge is the only one registered outside the SQL-stat
		// map loop, so it must be present even when no SQL has executed.
		Gauge gauge = registry.find("druid.sql.skip.count").gauge();
		assertNotNull(gauge,
				"druid.sql.skip.count must be registered regardless of executed SQL");
	}

	@Test
	void shouldAcceptNullSafeMapArgument() {
		// We deliberately do not mutate the map after passing it in, so the
		// binder must tolerate any map implementation including the empty one.
		DruidMetrics metrics = new DruidMetrics(new HashMap<>());
		metrics.bindTo(registry);

		assertEquals(0, registry.getMeters().size());
	}

	@Test
	void shouldCallJdbcDataSourceStatOverloadViaReflection() throws Exception {
		DruidMetrics metrics = new DruidMetrics(Collections.emptyMap());

		Method method = DruidMetrics.class.getDeclaredMethod(
				"bindDataSourceMetrics", MeterRegistry.class, JdbcDataSourceStat.class, List.class);
		method.setAccessible(true);

		JdbcDataSourceStat dsStats = new JdbcDataSourceStat("test", "test");
		List<Tag> tags = new ArrayList<>();
		tags.add(Tag.of("pool", "test"));

		method.invoke(metrics, registry, dsStats, tags);

		Gauge gauge = registry.find("druid.connection.active.count").gauge();
		assertNotNull(gauge,
				"druid.connection.active.count must be registered via the JdbcDataSourceStat overload");
	}

	@Test
	void shouldRegisterPerSqlGaugesWhenSqlStatMapIsPopulated() throws Exception {
		DruidDataSource dataSource = injectStat(new DruidDataSource());

		// Populate the SQL stat map via reflection so the per-SQL loop
		// in bindSqlMetrics is exercised (getSqlStatMap() may return
		// a defensive copy in some Druid versions).
		JdbcDataSourceStat dsStats = dataSource.getDataSourceStat();
		Field sqlStatMapField = JdbcDataSourceStat.class.getDeclaredField("sqlStatMap");
		sqlStatMapField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, JdbcSqlStat> sqlMap = (Map<String, JdbcSqlStat>) sqlStatMapField.get(dsStats);
		sqlMap.put("SELECT 1", new JdbcSqlStat("SELECT 1"));

		Map<String, DruidDataSource> dataSources = Collections.singletonMap("primary", dataSource);
		DruidMetrics metrics = new DruidMetrics(dataSources);
		metrics.bindTo(registry);

		Gauge executeCount = registry.find("druid.sql.execute.count").gauge();
		assertNotNull(executeCount,
				"per-SQL execute count gauge must be registered when the stat map is non-empty");
	}

	@Test
	void shouldRegisterMultiplePerSqlGaugesForDistinctStatements() throws Exception {
		DruidDataSource dataSource = injectStat(new DruidDataSource());

		JdbcDataSourceStat dsStats = dataSource.getDataSourceStat();
		Field sqlStatMapField = JdbcDataSourceStat.class.getDeclaredField("sqlStatMap");
		sqlStatMapField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, JdbcSqlStat> sqlMap = (Map<String, JdbcSqlStat>) sqlStatMapField.get(dsStats);
		sqlMap.put("SELECT 1", new JdbcSqlStat("SELECT 1"));
		sqlMap.put("INSERT INTO t VALUES (1)", new JdbcSqlStat("INSERT INTO t VALUES (1)"));

		Map<String, DruidDataSource> dataSources = Collections.singletonMap("primary", dataSource);
		DruidMetrics metrics = new DruidMetrics(dataSources);
		metrics.bindTo(registry);

		// Verify that both SQL tags are present.
		List<String> sqlTags = registry.getMeters().stream()
				.map(Meter::getId)
				.flatMap(id -> id.getTags().stream())
				.filter(tag -> "sql".equals(tag.getKey()))
				.map(Tag::getValue)
				.distinct()
				.collect(Collectors.toList());

		assertTrue(sqlTags.contains("SELECT 1"),
				"gauges for 'SELECT 1' SQL must be registered");
		assertTrue(sqlTags.contains("INSERT INTO t VALUES (1)"),
				"gauges for 'INSERT INTO t VALUES (1)' SQL must be registered");
	}

	@Test
	void shouldCallBindTimeGaugeViaReflection() throws Exception {
		DruidMetrics metrics = new DruidMetrics(Collections.emptyMap());

		// Find the private bindTimeGauge method.
		Method method = DruidMetrics.class.getDeclaredMethod(
				"bindTimeGauge", MeterRegistry.class, String.class, String.class,
				Object.class, ToDoubleFunction.class, Iterable.class);
		method.setAccessible(true);

		List<Tag> tags = new ArrayList<>();
		tags.add(Tag.of("pool", "test"));

		// Use a simple object whose method reference returns a double.
		JdbcDataSourceStat stat = new JdbcDataSourceStat("test", "test");

		@SuppressWarnings("unchecked")
		ToDoubleFunction<Object> measure = obj -> ((JdbcDataSourceStat) obj).getSkipSqlCount();
		method.invoke(metrics, registry, "druid.test.time.gauge", "Test time gauge",
				stat, measure, tags);

		Gauge gauge = registry.find("druid.test.time.gauge").gauge();
		assertNotNull(gauge,
				"the time gauge must be registered in the registry");
	}

	@Test
	void shouldCallBindTimerViaReflection() throws Exception {
		DruidMetrics metrics = new DruidMetrics(Collections.emptyMap());

		// Find the private bindTimer method.
		Method method = DruidMetrics.class.getDeclaredMethod(
				"bindTimer", MeterRegistry.class, String.class, String.class,
				Object.class, ToLongFunction.class, ToDoubleFunction.class, Iterable.class);
		method.setAccessible(true);

		List<Tag> tags = new ArrayList<>();
		tags.add(Tag.of("pool", "test"));

		JdbcDataSourceStat stat = new JdbcDataSourceStat("test", "test");

		@SuppressWarnings("unchecked")
		ToLongFunction<Object> countFunc = obj -> ((JdbcDataSourceStat) obj).getSkipSqlCount();
		@SuppressWarnings("unchecked")
		ToDoubleFunction<Object> measure = obj -> ((JdbcDataSourceStat) obj).getSkipSqlCount();
		method.invoke(metrics, registry, "druid.test.timer", "Test timer",
				stat, countFunc, measure, tags);

		// FunctionTimer registers as a FunctionTimer, not a plain Timer.
		assertNotNull(registry.find("druid.test.timer").functionTimer(),
				"the function timer must be registered in the registry");
	}

	@Test
	void shouldCallBindCounterViaReflection() throws Exception {
		DruidMetrics metrics = new DruidMetrics(Collections.emptyMap());

		// Find the private bindCounter method.
		Method method = DruidMetrics.class.getDeclaredMethod(
				"bindCounter", MeterRegistry.class, String.class, String.class,
				Object.class, ToDoubleFunction.class, Iterable.class);
		method.setAccessible(true);

		List<Tag> tags = new ArrayList<>();
		tags.add(Tag.of("pool", "test"));

		JdbcDataSourceStat stat = new JdbcDataSourceStat("test", "test");

		@SuppressWarnings("unchecked")
		ToDoubleFunction<Object> measure = obj -> ((JdbcDataSourceStat) obj).getSkipSqlCount();
		method.invoke(metrics, registry, "druid.test.counter", "Test counter",
				stat, measure, tags);

		// FunctionCounter registers as a FunctionCounter, not a plain Counter.
		assertNotNull(registry.find("druid.test.counter").functionCounter(),
				"the function counter must be registered in the registry");
	}

	/**
	 * Inject a fresh {@link JdbcDataSourceStat} into the {@code dataSourceStat}
	 * protected field of the supplied {@link DruidDataSource}. This avoids
	 * needing a real JDBC driver to initialise the pool.
	 *
	 * @param dataSource the pool to populate; never {@code null}.
	 * @return the same instance, mutated for testing convenience.
	 */
	private static DruidDataSource injectStat(DruidDataSource dataSource) {
		try {
			Field field = DruidDataSource.class.getDeclaredField("dataSourceStat");
			field.setAccessible(true);
			field.set(dataSource, new JdbcDataSourceStat("test", "test"));
			return dataSource;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(
					"unable to inject a JdbcDataSourceStat into the DruidDataSource", e);
		}
	}

	private static void assertGaugeValue(String name, double expected, MeterRegistry registry) {
		Gauge gauge = registry.find(name).gauge();
		assertNotNull(gauge, "expected a gauge named '" + name + "' to be registered");
		assertEquals(expected, gauge.value(), 0.0001d,
				"gauge '" + name + "' must report the configured value");
	}

}
