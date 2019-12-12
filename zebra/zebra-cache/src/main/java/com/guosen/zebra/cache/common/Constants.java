package com.guosen.zebra.cache.common;

/**
 * 常量类
 */
public final class Constants {
    private Constants(){}

    /**
     * 分布式缓存变更通知topic（注意和zebra-cache-sync微服务一致）
     */
    public static final String ROCKET_MQ_CACHE_CHANGE_TOPIC = "topic-zebra-cache-sync";
}
