/**   
* @Title: LocalRedisCacheManager.java 
* @Package com.guosen.zebra.core.cache 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年9月6日 下午5:49:16 
* @version V1.0   
*/
package com.guosen.zebra.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.guosen.zebra.cache.change.publisher.CacheChangePublisher;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: LocalRedisCacheManager
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2018年9月6日 下午5:49:16
 * 
 */
@SuppressWarnings("rawtypes")
public class LocalRedisCacheManager extends RedisCacheManager {
	private static final int INIT_CAPACITY = 5;

	private final RedisConnectionFactory connectionFactory;
	private final ZebraCacheConfig zebraCacheConfig;
	private final CacheChangePublisher cacheChangePublisher;

	public LocalRedisCacheManager(RedisCacheWriter cacheWriter,
								  RedisCacheConfiguration defaultCacheConfiguration,
								  RedisConnectionFactory connectionFactory,
								  CacheChangePublisher cacheChangePublisher,
								  ZebraCacheConfig zebraCacheConfig) {
		super(cacheWriter, defaultCacheConfiguration);
		this.connectionFactory = connectionFactory;
		this.cacheChangePublisher = cacheChangePublisher;
		this.zebraCacheConfig = zebraCacheConfig;
	}

	@Override
	protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
		// 每个缓存都对应一个CaffeineCache，但是redis是共用的(Redis中的key包含前缀cacheName::)。
		CaffeineCache localCache = buildLocalCache(name);

		RedisCacheConfiguration redisCacheConfiguration = cacheConfig != null ?
				cacheConfig : RedisCacheConfiguration.defaultCacheConfig();

		ZebraCache cache  = new ZebraCache(name,
				new DefaultRedisCacheWriter(connectionFactory),
				redisCacheConfiguration,
				localCache,
				cacheChangePublisher);
		cache.setEnableCacheTwo(zebraCacheConfig.getEnableCacheTwo());

		return cache;
	}

	private CaffeineCache buildLocalCache(String name) {
		return new CaffeineCache(name,
				Caffeine.newBuilder()
						.expireAfterWrite(zebraCacheConfig.getExpire(), TimeUnit.MILLISECONDS)
						.initialCapacity(INIT_CAPACITY)
						.maximumSize(zebraCacheConfig.getMaximumSize())
						.build());
	}
}
