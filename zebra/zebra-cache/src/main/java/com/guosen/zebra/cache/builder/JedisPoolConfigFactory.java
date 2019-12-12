package com.guosen.zebra.cache.builder;

import com.guosen.zebra.cache.ZebraCacheConfig;
import redis.clients.jedis.JedisPoolConfig;

/**
 * JedisPoolConfig工厂
 */
public final class JedisPoolConfigFactory {
    private JedisPoolConfigFactory(){}

    /**
     * 创建 JedisPoolConfig
     * @param zebraCacheConfig 缓存配置
     * @return JedisPoolConfig
     */
    public static JedisPoolConfig create(ZebraCacheConfig zebraCacheConfig) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 最大空闲数
        jedisPoolConfig.setMaxIdle(zebraCacheConfig.getMaxIdle());
        // 连接池的最大数据库连接数
        jedisPoolConfig.setMaxTotal(zebraCacheConfig.getMaxTotal());
        // 最大建立连接等待时间
        jedisPoolConfig.setMaxWaitMillis(zebraCacheConfig.getMaxWaitMillis());
        // 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        jedisPoolConfig.setMinEvictableIdleTimeMillis(zebraCacheConfig.getMinEvictableIdleTimeMillis());
        // 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        jedisPoolConfig.setNumTestsPerEvictionRun(zebraCacheConfig.getNumTestsPerEvictionRun());
        // 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(zebraCacheConfig.getTimeBetweenEvictionRunsMillis());
        // 是否在从池中取出连接前进行检验,如果检验失败,则从池中去除连接并尝试取出另一个
        jedisPoolConfig.setTestOnBorrow(zebraCacheConfig.getTestOnBorrow());
        // 在空闲时检查有效性, 默认false
        jedisPoolConfig.setTestWhileIdle(zebraCacheConfig.getTestWhileIdle());
        return jedisPoolConfig;
    }
}
