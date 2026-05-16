# druid-plus-spring-boot-starter


### 说明

> 基于druid 数据源的Spring Boot Starter 实现

1. DruidDataSource 自动初始化
2. 基于DruidDataSource的动态数据源实现
3. 基于AOP+注解实现数据源按需切换
4. DruidDataSource数据源监控逻辑
5. 基于[druid-spring-boot-starter](https://github.com/alibaba/druid/tree/master/druid-spring-boot-starter) 的扩展


### Maven

``` xml
<dependency>
	<groupId>io.github.hiwepy</groupId>
	<artifactId>druid-plus-spring-boot-starter</artifactId>
	<version>${project.version}</version>
</dependency>
```

#### Druid的数据源配置

##### 1、Druid DataSource 属性配置

Github Wiki: https://github.com/alibaba/druid/wiki/DruidDataSource%E9%85%8D%E7%BD%AE%E5%B1%9E%E6%80%A7%E5%88%97%E8%A1%A8

```yaml
spring:
  # 数据源配置：
  datasource:
    # Druid的数据源配置：
    druid:
      # 异步初始化策略，可加快启动速度。缺省值：false
      async-init: true
      # 初始化时建立物理连接的个数。初始化发生在显示调用init方法，或者第一次getConnection时；默认 15，推荐配置为：minIdle
      initial-size: 5
      # 最大连接池数量（按需配置）。缺省值：10；推荐的公式：((core_count * 2) + effective_spindle_count)，如我的服务器2核一个盘那就是 （2*2）+1=5
      # core_count: CPU核心数
      # effective_spindle_count is the number of disks in a RAID.就是磁盘列阵中的硬盘数
      # 例：虚拟机核心数量是8，磁盘是6磁盘的阵列，计算方式为：（8*2）+ 6 = 22
      max-active: 10
      # 获取连接时最大等待时间，单位毫秒。配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，如果需要可以通过配置useUnfairLock属性为true使用非公平锁
      max-wait: 60000
      # 最小连接池数量（按需配置）；默认 5
      min-idle: 5
      # 是否开启 keep-alive：即当最小空闲连接空闲了min-evictable-idle-time-millis，执行validationQuery进行keepAlive
      keep-alive: true
      # 设置连接最少存活时长和最大存活时长，超过上限才会被清理，需要注意满足(maxEvictableIdleTimeMillis-minEvictableIdleTimeMillis>timeBetweenEvictionRunsMillis)的条件
      # 连接保持空闲而不被驱逐的最小时间：5分钟，根据生产mysql配置的wait_time配置=5分钟
      min-evictable-idle-time-millis: 160000
      # 连接保持空闲而不被驱逐的最大时间: 2天，根据生产mysql配置的wait_time配置=2天
      max-evictable-idle-time-millis: 172800000
      # 超过时间限制是否回收
      remove-abandoned: true
      # 超过时间限制多长，单位：毫秒 ，180000毫秒=3分钟
      remove-abandoned-timeout-millis: 180000
      # 用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。
      validation-query: SELECT 1 FROM DUAL
      # 申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。建议配置为true，不影响性能，并且保证安全性。
      test-while-idle: true
      # 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒;
      # 有两个含义：
      #1) Destroy线程会检测连接的间隔时间，如果连接空闲时间大于等于minEvictableIdleTimeMillis则关闭物理连接。
      #2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
      time-between-eviction-runs-millis: 60000
      # 申请连接时执行validationQuery检测连接是否有效 ，做了这个配置会降低性能
      test-on-borrow: true
      # 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
      test-on-return: false
      # 是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql5.5以下的版本中没有PSCache功能，建议关闭掉。5.5及以上版本有PSCache，建议开启。
      pool-prepared-statements: true
      # 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
      max-pool-prepared-statement-per-connection-size: 100
      #打印druid统计信息：每天打印一次统计信息日志，后续根据日志帮助优化连接池配置和SQL（按需配置, -1表示关闭）
      time-between-log-stats-millis: 86400000
```

##### 2、Druid 监控界面访问配置

Druid内置提供了一个StatViewServlet用于展示Druid的统计信息。

这个StatViewServlet的用途包括：

- 提供监控信息展示的html页面
- 提供监控信息的JSON API

Github Wiki: https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_StatViewServlet%E9%85%8D%E7%BD%AE

> [danger] 注意：使用StatViewServlet，建议使用druid 0.2.6以上版本。

```yaml
spring:
  # 数据源配置：
  datasource:
    druid:
      # 监控界面访问配置：https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_StatViewServlet%E9%85%8D%E7%BD%AE
      stat-view-servlet:
        # 是否开启，Druid 监控页面功能
        enabled: true
        # Druid 监控页面访问地址
        url-pattern: /druid/*
        # Druid 监控页面登录用户名
        login-username: admin
        # Druid 监控页面登录密码
        login-password: admin
        # 是否允许清空统计数据 默认值：false
        reset-enable: false
        # IP 白名单（没有配置或者为空，则允许所有访问）
        allow: 192.168.3.1/24
```

##### 3、Druid和Spring关联监控配置

> [danger] Druid提供了Spring和Jdbc的关联监控。

Github Wiki: https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_Druid%E5%92%8CSpring%E5%85%B3%E8%81%94%E7%9B%91%E6%8E%A7%E9%85%8D%E7%BD%AE


```yaml
spring:
  # 数据源配置：
  datasource:
    druid:
      # Druid和Spring关联监控配置：https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_Druid%E5%92%8CSpring%E5%85%B3%E8%81%94%E7%9B%91%E6%8E%A7%E9%85%8D%E7%BD%AE
      aop-patterns: com.**.service.**.*Service
```

##### 4、Druid Session 状态信息采集配置

> [danger] WebStatFilter用于采集web-jdbc关联监控的数据。

Github Wiki: https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_%E9%85%8D%E7%BD%AEWebStatFilter

```yaml
spring:
  # 数据源配置：
  datasource:
    druid:
      # WebStatFilter 配置：https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_%E9%85%8D%E7%BD%AEWebStatFilter
      web-stat-filter:
        # 是否启用 WebStatFilter，默认值：false
        enabled: true
        # 是否启用 WebStatFilter 的 URL Pattern，默认值：/*，即所有请求都会被拦截
        url-pattern: /*
        # WebStatFilter 忽略拦截的路径表达式，默认值：*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*
        exclusions: '*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*'
        # 是否启用 Session 统计功能，默认值：false
        session-stat-enable: true
        # 最大允许统计多少个 Session
        session-stat-max-count: 20000
        # 设置 druid 从 Session 中存获取当前的 user 信息的 Key，注意：如果你 session 中保存的是非 string 类型的对象，需要重载 toString 方法，否则会报错
        principal-session-name: 'xxx.user'
        # 如果你的 user 信息保存在 cookie 中，你可以配置 principalCookieName，根据需要，把其中的 xxx.user 修改为你 user 信息保存在 cookie 中的 cookieName
        principal-cookie-name: 'xxx.user'
        # druid 0.2.7版本开始支持profile，配置 profileEnable 能够监控单个 url 调用的 sql 列表
        profile-enable: true
```

##### 5、Druid 慢SQL统计

> [danger] Druid内置提供一个StatFilter，用于统计监控信息。

Github Wiki: https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_StatFilter

```yaml
spring:
  # 数据源配置：
  datasource:
    druid:
      # Druid的监控统计功能:属性类型是字符串，通过别名的方式配置扩展插件
      filters: mergeStat
      # Druid 自定义过滤器参数
      filter:
        # 慢SQL统计 ：https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_StatFilter
        stat:
          # 是否开启 StatFilter，默认值：false
          enabled: true
          # 是否启用连接池中的连接获取栈信息。如果将其设置为 true，则在日志中会输出详细的连接获取栈信息，方便调试和分析连接使用情况。如果设置为 false，则不会输出这些信息。默认值为 false。
          connection-stack-trace-enable: true
          # 是否启用慢SQL记录，默认值：false
          log-slow-sql: true
          # 用于设置 SQL 执行时间超过多少毫秒时认为是慢查询。如果为 true 则会在日志中输出执行时间超过这个阈值的 SQL 语句及其执行时间。
          slow-sql-millis: 3000
          # 慢SQL日志级别：DEBUG、INFO、WARN、ERROR，默认值：ERROR
          slow-sql-log-level: "ERROR"
          # 是否启用SQL统计合并功能，默认值：false
          merge-sql: true
```

##### 6、Druid 使用slf4j进行日志输出

> [danger] 可以通过Slf4j的配置使用Log4j2日志组件进行日志的打印。

Github Wiki: https://github.com/alibaba/druid/wiki/Druid%E4%B8%AD%E4%BD%BF%E7%94%A8log4j2%E8%BF%9B%E8%A1%8C%E6%97%A5%E5%BF%97%E8%BE%93%E5%87%BA

```yaml
spring:
  # 数据源配置：
  datasource:
    druid:
      # Druid的监控统计功能:属性类型是字符串，通过别名的方式配置扩展插件
      filters: mergeStat,slf4j
      # Druid 自定义过滤器参数
      filter:
        # Druid中使用log4j2进行日志输出：https://github.com/alibaba/druid/wiki/Druid%E4%B8%AD%E4%BD%BF%E7%94%A8log4j2%E8%BF%9B%E8%A1%8C%E6%97%A5%E5%BF%97%E8%BE%93%E5%87%BA
        slf4j:
          # 是否开启日志输出
          enabled: true
          # Statement 创建后是否打印日志
          statement-create-after-log-enabled: false
          # Statement 关闭后是否打印日志
          statement-close-after-log-enabled: false
          # ResultSet 打开后是否打印日志
          result-set-open-after-log-enabled: false
          # ResultSet 关闭后是否打印日志
          result-set-close-after-log-enabled: false
```

##### 7、Druid 配置安全防护配置

> [danger] 开启 Wallfilter 功能，并根据生产需求进行配置，可有效的进行精细化的安全防护，提前屏蔽掉一些潜在的风险。

Github Wiki: https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE-wallfilter

```yaml
spring:
  # 数据源配置：
  datasource:
    druid:
      # Druid的监控统计功能:属性类型是字符串，通过别名的方式配置扩展插件
      filters: mergeStat,slf4j,wall
      # Druid 自定义过滤器参数
      filter:
        # Druid防御SQL注入：https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE-wallfilter
        wall:
          # 对被认为是攻击的SQL进行LOG.error输出
          log-violation: true
          # 对被认为是攻击的SQL抛出SQLExcepton
          throw-exception: true
          # 配置白名单
          config:
            # 是否允许语句中存在注释，Oracle的用户不用担心，Wall能够识别hints和注释的区别
            comment-allow: true
            # 是否允许非以上基本语句的其他语句，缺省关闭，通过这个选项就能够屏蔽DDL
            none-base-statement-allow: true
            # 是否允许一次执行多条语句，缺省关闭
            multi-statement-allow: true
            # 解除union关键字检查
            select-union-check: false
            # 是否允许执行锁定表操作
            lock-table-allow: true
```

### Sample

[https://github.com/vindell/spring-boot-starter-samples/tree/master/spring-boot-sample-druid](https://github.com/vindell/spring-boot-starter-samples/tree/master/spring-boot-sample-druid "spring-boot-sample-druid")