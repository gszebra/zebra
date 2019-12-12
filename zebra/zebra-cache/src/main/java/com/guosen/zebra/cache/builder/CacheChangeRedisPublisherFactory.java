package com.guosen.zebra.cache.builder;

import com.guosen.zebra.cache.ZebraCacheConfig;
import com.guosen.zebra.cache.change.publisher.CacheChangeRedisPublisher;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * CacheChangeRedisPublisher工厂
 */
public final class CacheChangeRedisPublisherFactory {
    private CacheChangeRedisPublisherFactory(){}

    /**
     * 构建CacheChangeRedisPublisher
     * @param zebraCacheConfig  缓存配置
     * @param redisTemplate     Spring Redis 模板
     * @return  CacheChangeRedisPublisher
     */
    public static CacheChangeRedisPublisher create(ZebraCacheConfig zebraCacheConfig,
                                                   RedisTemplate<String, Object> redisTemplate) {
        String topicName = zebraCacheConfig.getTopicName();
        return new CacheChangeRedisPublisher(topicName, redisTemplate);
    }
}
