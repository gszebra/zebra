package com.guosen.zebra.cache.builder;

import com.guosen.zebra.cache.LocalRedisCacheManagerFactory;
import com.guosen.zebra.cache.ZebraCacheConfig;
import com.guosen.zebra.cache.change.publisher.CacheChangeNoneOpPublisher;
import com.guosen.zebra.cache.change.publisher.CacheChangePublisher;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

/**
 * FirstCacheManager工厂类
 */
public final class FirstCacheManagerFactory {
    private FirstCacheManagerFactory(){}

    /**
     * 构建FirstCacheManager
     * @param zebraCacheConfig  缓存配置
     * @return FirstCacheManager
     */
    public static CacheManager create(ZebraCacheConfig zebraCacheConfig) {
        RedisCacheConfiguration conf = RedisCacheConfiguration.defaultCacheConfig();
        conf = conf.entryTtl(Duration.ofMillis(zebraCacheConfig.getExpire()));

        // 一级缓存无须通知，使用NoneOp
        CacheChangePublisher cacheChangePublisher = new CacheChangeNoneOpPublisher();

        return LocalRedisCacheManagerFactory.createFirst(conf, cacheChangePublisher, zebraCacheConfig);
    }
}
