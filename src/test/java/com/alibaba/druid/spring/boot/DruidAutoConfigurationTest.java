package com.alibaba.druid.spring.boot;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.config.ConfigFilter;
import com.alibaba.druid.filter.encoding.EncodingConvertFilter;
import com.alibaba.druid.filter.logging.CommonsLogFilter;
import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.filter.logging.Log4jFilter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.ds.DruidDataSourceProperties;
import com.alibaba.druid.spring.boot.ds.DynamicRoutingDataSource;
import com.alibaba.druid.spring.boot.ds.filter.FrameStatFilter;
import com.alibaba.druid.spring.boot.ds.filter.FrameWallFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import javax.sql.DataSource;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class DruidAutoConfigurationTest {

    private final DruidAutoConfiguration configuration = new DruidAutoConfiguration();

    @Test
    void createsFrameFiltersWithTheSuppliedWallConfiguration() {
        WallConfig wallConfig = new WallConfig();
        WallFilter wallFilter = configuration.wallFilter(wallConfig);
        assertThat(wallFilter).isInstanceOf(FrameWallFilter.class);
        assertThat(wallFilter.getConfig()).isSameAs(wallConfig);
        assertThat(configuration.statFilter()).isInstanceOf(FrameStatFilter.class);
    }

    @Test
    void createsPlainAndRoutableDataSources() {
        DataSourceProperties basic = new DataSourceProperties();
        basic.setUrl("jdbc:h2:mem:druid_auto;DB_CLOSE_DELAY=-1");
        basic.setUsername("sa");
        basic.setPassword("");

        DruidProperties plain = new DruidProperties();
        plain.setUrl("jdbc:h2:mem:druid_plain;DB_CLOSE_DELAY=-1");
        plain.setUsername("plain-user");
        plain.setPassword("plain-password");
        StatFilter plainStatFilter = new StatFilter();
        DataSource plainDataSource = configuration.dataSource(basic, plain,
                provider(StatFilter.class, plainStatFilter), emptyProvider(ConfigFilter.class),
                emptyProvider(EncodingConvertFilter.class), emptyProvider(Slf4jLogFilter.class),
                emptyProvider(Log4jFilter.class), emptyProvider(Log4j2Filter.class),
                emptyProvider(CommonsLogFilter.class), emptyProvider(WallFilter.class));
        assertThat(plainDataSource).isInstanceOf(DruidDataSource.class);
        DruidDataSource configuredPlain = (DruidDataSource) plainDataSource;
        assertThat(configuredPlain.getUrl()).isEqualTo("jdbc:h2:mem:druid_plain;DB_CLOSE_DELAY=-1");
        assertThat(configuredPlain.getUsername()).isEqualTo("plain-user");
        assertThat(configuredPlain.getPassword()).isEqualTo("plain-password");
        assertThat(configuredPlain.getProxyFilters()).contains((Filter) plainStatFilter);

        DruidProperties routable = new DruidProperties();
        routable.setRoutable(true);
        routable.setUrl(basic.getUrl());
        routable.setUsername("sa");
        routable.setPassword("");
        DruidDataSourceProperties slave = new DruidDataSourceProperties();
        slave.setName("reporting");
        slave.setUrl("jdbc:h2:mem:druid_slave;DB_CLOSE_DELAY=-1");
        slave.setUsername("sa");
        slave.setPassword("");
        routable.setSlaves(Collections.singletonList(slave));

        StatFilter statFilter = new StatFilter();
        DataSource routing = configuration.dataSource(basic, routable,
                provider(StatFilter.class, statFilter), emptyProvider(ConfigFilter.class),
                emptyProvider(EncodingConvertFilter.class), emptyProvider(Slf4jLogFilter.class),
                emptyProvider(Log4jFilter.class), emptyProvider(Log4j2Filter.class),
                emptyProvider(CommonsLogFilter.class), emptyProvider(WallFilter.class));
        assertThat(routing).isInstanceOf(DynamicRoutingDataSource.class);
        DynamicRoutingDataSource dynamic = (DynamicRoutingDataSource) routing;
        assertThat(dynamic.getTargetDataSources()).containsKey("reporting");
        DruidDataSource reporting = (DruidDataSource) dynamic.getTargetDataSources().get("reporting");
        assertThat(reporting.getProxyFilters()).contains((Filter) statFilter);
    }

    @Test
    void autoConfigurationBindsPropertiesAndHonorsEnabledCondition() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(DruidAutoConfiguration.class))
                .withPropertyValues(
                        "spring.datasource.url=jdbc:h2:mem:druid_context;DB_CLOSE_DELAY=-1",
                        "spring.datasource.username=sa",
                        "spring.datasource.password=",
                        "spring.datasource.druid.url=jdbc:h2:mem:druid_bound;DB_CLOSE_DELAY=-1",
                        "spring.datasource.druid.username=bound-user",
                        "spring.datasource.druid.password=bound-password");

        runner.run(context -> {
            assertThat(context).hasSingleBean(DataSource.class);
            DruidDataSource dataSource = context.getBean(DruidDataSource.class);
            assertThat(dataSource.getUrl()).isEqualTo("jdbc:h2:mem:druid_bound;DB_CLOSE_DELAY=-1");
            assertThat(dataSource.getUsername()).isEqualTo("bound-user");
            assertThat(dataSource.getPassword()).isEqualTo("bound-password");
        });

        runner.withPropertyValues("spring.datasource.druid.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(DataSource.class));
    }

    private <T> ObjectProvider<T> emptyProvider(Class<T> type) {
        return new StaticListableBeanFactory().getBeanProvider(type);
    }

    private <T> ObjectProvider<T> provider(Class<T> type, T bean) {
        StaticListableBeanFactory factory = new StaticListableBeanFactory();
        factory.addBean(type.getName(), bean);
        return factory.getBeanProvider(type);
    }
}
