package com.guosen.zebra.cache.builder;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.guosen.zebra.cache.LocalRedisCacheManagerFactory;
import com.guosen.zebra.cache.ZebraCacheConfig;
import com.guosen.zebra.cache.change.publisher.CacheChangePublisher;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * 二级缓存工厂类
 */
public final class SecondaryCacheManagerFactory {

    private SecondaryCacheManagerFactory(){}

    /**
     * 构建二级缓存
     * @return  二级缓存
     */
    public static CacheManager create(ZebraCacheConfig zebraCacheConfig,
                                      JedisConnectionFactory jedisConnectionFactory,
                                      CacheChangePublisher cacheChangePublisher) {
        StringRedisSerializer serializer = new StringRedisSerializer();
        GenericFastJsonRedisSerializer fastSerializer = new GenericFastJsonRedisSerializer();
        RedisCacheConfiguration conf = RedisCacheConfiguration.defaultCacheConfig();
        conf = conf.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(fastSerializer))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .entryTtl(Duration.ofMillis(zebraCacheConfig.getExpire()));

        return LocalRedisCacheManagerFactory.createSecondary(jedisConnectionFactory, conf,
                cacheChangePublisher, zebraCacheConfig);
    }
}
