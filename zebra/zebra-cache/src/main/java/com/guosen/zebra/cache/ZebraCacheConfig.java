/**   
* @Title: CacheConfig.java 
* @Package com.guosen.zebra.core.cache 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年9月6日 下午5:55:54 
* @version V1.0   
*/
package com.guosen.zebra.cache;

import com.guosen.zebra.cache.builder.*;
import com.guosen.zebra.cache.change.consumer.RocketMqConsumerAdapter;
import com.guosen.zebra.cache.change.publisher.CacheChangePublisher;
import com.guosen.zebra.cache.change.publisher.CacheChangeRocketMqPublisher;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @ClassName: ZebraCacheConfig
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2018年9月6日 下午5:55:54
 * 
 */
@Configuration
@ConditionalOnProperty(name = "zebra.cache.enable", havingValue = "true")
@EnableCaching
public class ZebraCacheConfig {
	@Value("${zebra.cache.redis.topic:zebraTopic}")
	private String topicName;

	@Value("${zebra.cache.enable.two:false}")
	private boolean enableCacheTwo;

	@Value("${redis.timeout:1000}")
	private Long timeout;

	@Value("${redis.database:0}")
	private int database;

	@Value("${redis.hostName:}")
	private String host;

	@Value("${redis.port:0}")
	private Integer port;

	@Value("${redis.password:}")
	private String password;

	@Value("${redis.maxIdle:0}")
	private Integer maxIdle;

	@Value("${redis.maxTotal:0}")
	private Integer maxTotal;

	@Value("${redis.maxWaitMillis:0}")
	private Integer maxWaitMillis;

	@Value("${redis.minEvictableIdleTimeMillis:0}")
	private Integer minEvictableIdleTimeMillis;

	@Value("${redis.numTestsPerEvictionRun:0}")
	private Integer numTestsPerEvictionRun;

	@Value("${redis.timeBetweenEvictionRunsMillis:0}")
	private long timeBetweenEvictionRunsMillis;

	@Value("${redis.testOnBorrow:false}")
	private boolean testOnBorrow;

	@Value("${redis.testWhileIdle:false}")
	private boolean testWhileIdle;

	@Value("${spring.redis.cluster.nodes:}")
	private String clusterNodes;

	@Value("${spring.redis.cluster.max-redirects:3}")
	private Integer maxRedirects;

	@Value("${cache.expire:1800000}")
	private Long expire;

	@Value("${redis.readTimeout:1000}")
	private Long readTimeout;

	@Value("${cache.maximumSize:10000}")
	private Long maximumSize;

	/**
	 * 是否启用跨数据中心缓存同步
	 */
	@Value("${zebra.cache.sync.dc.enable:false}")
	private boolean enableDcSync;

	/**
	 * 接收和发送缓存变更通知的RocketMQ name server
	 */
	@Value("${zebra.cache.sync.mq.namesrvAddr:}")
	private String rocketMqNameSrv;

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public boolean getEnableCacheTwo() {
		return enableCacheTwo;
	}

	public void setEnableCacheTwo(boolean enableCacheTwo) {
		this.enableCacheTwo = enableCacheTwo;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public int getDatabase() {
		return database;
	}

	public void setDatabase(int database) {
		this.database = database;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
	}

	public Integer getMaxTotal() {
		return maxTotal;
	}

	public void setMaxTotal(Integer maxTotal) {
		this.maxTotal = maxTotal;
	}

	public Integer getMaxWaitMillis() {
		return maxWaitMillis;
	}

	public void setMaxWaitMillis(Integer maxWaitMillis) {
		this.maxWaitMillis = maxWaitMillis;
	}

	public Integer getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(Integer minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public Integer getNumTestsPerEvictionRun() {
		return numTestsPerEvictionRun;
	}

	public void setNumTestsPerEvictionRun(Integer numTestsPerEvictionRun) {
		this.numTestsPerEvictionRun = numTestsPerEvictionRun;
	}

	public long getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	public boolean getTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public boolean getTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public String getClusterNodes() {
		return clusterNodes;
	}

	public void setClusterNodes(String clusterNodes) {
		this.clusterNodes = clusterNodes;
	}

	public Integer getMaxRedirects() {
		return maxRedirects;
	}

	public void setMaxRedirects(Integer maxRedirects) {
		this.maxRedirects = maxRedirects;
	}

	public Long getExpire() {
		return expire;
	}

	public void setExpire(Long expire) {
		this.expire = expire;
	}

	public Long getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(Long readTimeout) {
		this.readTimeout = readTimeout;
	}

	public Long getMaximumSize() {
		return maximumSize;
	}

	public void setMaximumSize(Long maximumSize) {
		this.maximumSize = maximumSize;
	}

	public boolean getEnableDcSync() {
		return enableDcSync;
	}

	public void setEnableDcSync(boolean enableDcSync) {
		this.enableDcSync = enableDcSync;
	}

	public String getRocketMqNameSrv() {
		return rocketMqNameSrv;
	}

	public void setRocketMqNameSrv(String rocketMqNameSrv) {
		this.rocketMqNameSrv = rocketMqNameSrv;
	}


	@Bean
	@ConditionalOnProperty(name = "zebra.cache.enable.two", havingValue = "true")
	public JedisPoolConfig jedisPoolConfig() {
		return JedisPoolConfigFactory.create(this);
	}

	@Bean
	@ConditionalOnBean(JedisPoolConfig.class)
	public JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig jedisPoolConfig) {
		return JedisConnectionFactoryFactory.create(this, jedisPoolConfig);
	}

	@Bean
	@ConditionalOnBean(JedisConnectionFactory.class)
	public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
		return RedisTemplateFactory.create(jedisConnectionFactory);
	}

	@Bean
	@ConditionalOnBean(JedisConnectionFactory.class)
	@ConditionalOnProperty(name = "zebra.cache.sync.dc.enable", havingValue = "false")
	public CacheChangePublisher cacheChangeRedisPublisher(RedisTemplate<String, Object> redisTemplate) {
		return CacheChangeRedisPublisherFactory.create(this, redisTemplate);
	}

	@Bean
	@Primary
	@ConditionalOnMissingBean(JedisConnectionFactory.class)
	public CacheManager firstCacheManager() {
		return FirstCacheManagerFactory.create(this);
	}

	@Bean
	@Primary
	@ConditionalOnBean(JedisConnectionFactory.class)
	public CacheManager secondaryCacheManager(JedisConnectionFactory jedisConnectionFactory, CacheChangePublisher cacheChangePublisher) {
		return SecondaryCacheManagerFactory.create(this, jedisConnectionFactory, cacheChangePublisher);
	}

	@Bean
	@ConditionalOnBean(RedisTemplate.class)
	@ConditionalOnProperty(name = "zebra.cache.sync.dc.enable", havingValue = "false")
	public MessageListenerAdapter listenerAdapter(LocalRedisCacheManager cacheManager,
			RedisTemplate<String, Object> redisTemplate) {
		return MessageListenerAdapterFactory.create(cacheManager, redisTemplate);
	}

	@Bean
	@ConditionalOnBean(MessageListenerAdapter.class)
	public RedisMessageListenerContainer container(JedisConnectionFactory jedisConnectionFactory,
												   MessageListenerAdapter listenerAdapter) {
		return RedisMessageListenerContainerFactory.create(this, jedisConnectionFactory, listenerAdapter);
	}


	@Bean(name = "zebraCacheSyncDefaultMQProducer")
	@ConditionalOnProperty(name = "zebra.cache.sync.dc.enable", havingValue = "true")
	public DefaultMQProducer mqProducer() {
		return DefaultMQProducerFactory.create(this);
	}

	@Bean
	@ConditionalOnBean(name = "zebraCacheSyncDefaultMQProducer")
	@ConditionalOnProperty(name = "zebra.cache.sync.dc.enable", havingValue = "true")
	public CacheChangePublisher cacheChangeRocketMqPublisher(DefaultMQProducer zebraCacheSyncDefaultMQProducer) {
		return new CacheChangeRocketMqPublisher(zebraCacheSyncDefaultMQProducer);
	}

	@Bean()
	@ConditionalOnProperty(name = "zebra.cache.sync.dc.enable", havingValue = "true")
	public RocketMqConsumerAdapter rocketMqConsumer(LocalRedisCacheManager cacheManager) {
		return RocketMqConsumerAdaptorFactory.create(this, cacheManager);
	}
}
