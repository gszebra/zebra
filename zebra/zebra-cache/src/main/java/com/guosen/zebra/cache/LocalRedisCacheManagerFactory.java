package com.guosen.zebra.cache;

import com.guosen.zebra.cache.change.publisher.CacheChangePublisher;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 缓存管理器工厂
 */
public final class LocalRedisCacheManagerFactory {
    private LocalRedisCacheManagerFactory(){}

    /**
     * 创建一级缓存管理器
     * @return 一级缓存管理器
     */
    public static LocalRedisCacheManager createFirst(RedisCacheConfiguration redisConf,
                                                     CacheChangePublisher cacheChangePublisher,
                                                     ZebraCacheConfig zebraCacheConfig) {
        return new LocalRedisCacheManager(
                new DefaultRedisCacheWriter(null),
                redisConf,
                null,
                cacheChangePublisher,
                zebraCacheConfig);
    }

    /**
     * 创建二级缓存管理器
     * @return 二级缓存管理器
     */
    public static LocalRedisCacheManager createSecondary(RedisConnectionFactory connectionFactory,
                                                         RedisCacheConfiguration redisConf,
                                                         CacheChangePublisher cacheChangePublisher,
                                                         ZebraCacheConfig zebraCacheConfig) {
        return new LocalRedisCacheManager(
                new DefaultRedisCacheWriter(connectionFactory),
                redisConf,
                connectionFactory,
                cacheChangePublisher,
                zebraCacheConfig);
    }
}
