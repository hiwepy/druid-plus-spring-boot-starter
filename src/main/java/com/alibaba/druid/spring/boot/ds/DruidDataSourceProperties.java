package com.alibaba.druid.spring.boot.ds;

import java.util.List;
import java.util.Properties;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.util.StringUtils;

import com.alibaba.druid.pool.DruidAbstractDataSource;
import com.alibaba.druid.util.JdbcUtils;

import lombok.Data;

@SuppressWarnings("serial")
@Data
/**
 * <p>Auto-configuration for DruidDataSourceProperties.</p>
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class DruidDataSourceProperties {

	/** 基本属性 url、user、password */

	private String driverClassName;
	/**
	 * 配置这个属性的意义在于，如果存在多个数据源，监控的时候可以通过名字来区分开来。如果没有配置，将会生成一个名字，格式是：”DataSource-” +
	 * System.identityHashCode(this)
	 */
	private String name;
	/** jdbcUrl: 连接数据库的url，不同数据库不一样 */
	private String url;
	/** username: 连接数据库的用户名 */
	private String username;
	/** password: 连接数据库的密码 */
	private String password;

	/** connectionProperties: 连接数据库的额外参数 */
	private Properties connectionProperties = new Properties() {
		{
			put("druid.stat.mergeSql", "true");
			put("druid.stat.slowSqlMillis", "5000");
		}
	};

	private String dbType;

	/** druid 连接池参数 */
	
	private boolean accessToUnderlyingConnectionAllowed = true;
	private boolean asyncCloseConnectionEnable = false;
	private boolean asyncInit = false;
	private boolean checkExecuteTime = false;
	private boolean clearFiltersEnable = true;
	
	private boolean defaultAutoCommit = true;
	private boolean defaultReadOnly;
	private Integer defaultTransactionIsolation;
	private String  defaultCatalog = null;
	private boolean dupCloseLogEnable = false;

	private boolean failFast;
	private List<String> connectionInitSqls;
	private boolean initExceptionThrow = true;
	private boolean initGlobalVariants = false;
	private boolean initVariants = false;
	private boolean keepAlive;
	private long keepAliveBetweenTimeMillis = DruidAbstractDataSource.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS * 2;
	private boolean killWhenSocketReadTimeout = false;

	private boolean logAbandoned;
	private boolean logDifferentThread = true;
	private int loginTimeout = 0;
	
	/**
	 * 获取连接时最大等待时间，单位毫秒。配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，如果需要可以通过配置useUnfairLock属性为true使用非公平锁。
	 */
	private Integer maxWait = DruidAbstractDataSource.DEFAULT_MAX_WAIT;
	private Integer maxWaitThreadCount = -1;

	private Integer notFullTimeoutRetryCount = 0;
	
	private Long phyTimeoutMillis = DruidAbstractDataSource.DEFAULT_PHY_TIMEOUT_MILLIS;
	private Long phyMaxUseCount = -1L;
	
	/** 配置初始化大小、最小、最大 连接池数量 */

	/**
	 * 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位：毫秒;有两个含义：1) Destroy线程会检测连接的间隔时间 2)
	 * testWhileIdle的判断依据，详细看testWhileIdle属性的说明
	 */
	private Long timeBetweenEvictionRunsMillis = DruidAbstractDataSource.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
	private Integer maxOpenPreparedStatements = -1;
	private Integer numTestsPerEvictionRun = DruidAbstractDataSource.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;

	/** 超过时间限制是否回收 */
	private boolean removeAbandoned;
	/** 超过时间限制多久触发回收逻辑，单位：毫秒 ，180000毫秒=3分钟 */
	private Long removeAbandonedTimeoutMillis = 300 * 1000L;
	 private boolean  resetStatEnable           = true;
	 
	 
	private Integer connectionErrorRetryAttempts = 1;
	private boolean breakAfterAcquireFailure = false;

	private String connectProperties;
	/** maxActive: 连接池最大连接数量 */
	private Integer maxActive = DruidAbstractDataSource.DEFAULT_MAX_ACTIVE_SIZE;
	private int maxCreateTaskCount                        = 3;
	
	private int queryTimeout;

	private Integer maxIdle = DruidAbstractDataSource.DEFAULT_MAX_IDLE;
	/** minIdle: 连接池最小连接数量 */
	private Integer minIdle = DruidAbstractDataSource.DEFAULT_MIN_IDLE;
	/**
	 * 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
	 */
	private Integer maxPoolPreparedStatementPerConnectionSize = 10;

	/** 配置一个连接在池中最大生存的时间，单位:毫秒 */
	private Long maxEvictableIdleTimeMillis = DruidAbstractDataSource.DEFAULT_MAX_EVICTABLE_IDLE_TIME_MILLIS;
	/** 配置一个连接在池中最小生存的时间，单位：毫秒 */
	private Long minEvictableIdleTimeMillis = DruidAbstractDataSource.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
	

	/**
	 * Druid的监控统计功能:属性类型是字符串，通过别名的方式配置扩展插件，常用的插件有： #监控统计用的filter:stat
	 * #日志用的filter:slf4j #防御SQL注入的filter:wall
	 * 开启Druid的监控统计功能，mergeStat代替stat表示sql合并,wall表示防御SQL注入攻击
	 */
	private String filters = "mergeStat,wall,slf4j";
	
	/**
	 * 是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql5.5以下的版本中没有PSCache功能，建议关闭掉。5.5及以上版本有PSCache，建议开启。
	 */
	private boolean poolPreparedStatements = false;
	private Integer statSqlMaxSize;
	private boolean sharePreparedStatements = false;
	/**
	 * 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
	 */
	private boolean testWhileIdle = DruidAbstractDataSource.DEFAULT_WHILE_IDLE;
	/** 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。 */
	private boolean testOnBorrow = DruidAbstractDataSource.DEFAULT_TEST_ON_BORROW;
	/** 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能 */
	private boolean testOnReturn = DruidAbstractDataSource.DEFAULT_TEST_ON_RETURN;
	private Long timeBetweenConnectErrorMillis = DruidAbstractDataSource.DEFAULT_TIME_BETWEEN_CONNECT_ERROR_MILLIS;
	private Long timeBetweenLogStatsMillis;
	private int transactionQueryTimeout;
	private Long transactionThresholdMillis = 0L;
	
	/** initialSize: 初始化时建立物理连接的个数。初始化发生在显示调用init方法，或者第一次getConnection时 */
	private Integer initialSize = DruidAbstractDataSource.DEFAULT_INITIAL_SIZE;
	private boolean useUnfairLock;
	private boolean useLocalSessionState = true;
	private boolean useGlobalDataSourceStat;
	/**
	 * 用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。
	 */
	private String validationQuery = "SELECT 1";
	private Integer validationQueryTimeout = -1;

	/** @return return the db type. */
	public String getDbType() {
		return StringUtils.hasText(dbType) ? dbType : JdbcUtils.getDbType(this.getUrl(), null);
	}

	/**
	 * <p>To properties.</p>
	 * @return the result
	 */
	public Properties toProperties() {

		Properties properties = new Properties();

		notNullAdd(properties, "name", this.name);
		notNullAdd(properties, "driverClassName", this.driverClassName);
		notNullAdd(properties, "url", this.url);
		notNullAdd(properties, "username", this.username);
		notNullAdd(properties, "password", this.password);
		notNullAdd(properties, "stat.sql.MaxSize", this.statSqlMaxSize);
		
		return properties;
	}

	/**
	 * <p>Not null add.</p>
	 * @param properties
	 * @param key
	 * @param value
	 */
	protected void notNullAdd(Properties properties, String key, Object value) {
		if (value != null) {
			properties.setProperty("druid." + key, value.toString());
		}
	}
	
	
	/**
	 * <p>Configure properties.</p>
	 * @param basicProperties
	 * @return the result
	 */
	public DruidDataSourceProperties configureProperties(DataSourceProperties basicProperties) {
		 //if not found prefix 'spring.datasource.druid' jdbc properties ,'spring.datasource' prefix jdbc properties will be used.
		if (this.getName() == null) {
        	this.setName(basicProperties.getName());
        }
		if (this.getUsername() == null) {
        	this.setUsername(basicProperties.determineUsername());
        }
        if (this.getPassword() == null) {
        	this.setPassword(basicProperties.determinePassword());
        }
        if (this.getUrl() == null) {
        	this.setUrl(basicProperties.determineUrl());
        }
        if(this.getDriverClassName() == null){
        	this.setDriverClassName(basicProperties.determineDriverClassName());
        }
        return this;
	}

}