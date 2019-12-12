package com.guosen.zebra.cache.builder;

import com.guosen.zebra.cache.ZebraCacheConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.Arrays;

/**
 * JedisConnectionFactory工厂
 */
public final class JedisConnectionFactoryFactory {
    /**
     * 客户端名称
     */
    private static final String CLIENT_NAME = "zebra";

    private JedisConnectionFactoryFactory(){}

    /**
     * 创建JedisConnectionFactory
     * @param zebraCacheConfig  缓存配置
     * @param jedisPoolConfig   redis pool配置
     * @return JedisConnectionFactory
     */
    public static JedisConnectionFactory create(ZebraCacheConfig zebraCacheConfig, JedisPoolConfig jedisPoolConfig) {
        String clusterNodes = zebraCacheConfig.getClusterNodes();
        String host = zebraCacheConfig.getHost();
        int maxRedirects = zebraCacheConfig.getMaxRedirects();

        JedisClientConfiguration clientConfig = getJedisClientConfiguration(zebraCacheConfig, jedisPoolConfig);

        boolean isClusterNodeConfig = StringUtils.isNotEmpty(clusterNodes);
        boolean isHostConfig = StringUtils.isNotEmpty(host);

        JedisConnectionFactory jedisConnectionFactory = null;
        if (!isClusterNodeConfig && !isHostConfig) {
            jedisConnectionFactory = new JedisConnectionFactory();
        }
        else if (!isClusterNodeConfig) {
            jedisConnectionFactory = getJedisConnectionFactory(host, zebraCacheConfig, clientConfig);
        }
        else {
            jedisConnectionFactory = getRedisClusterConfiguration(clusterNodes, maxRedirects, clientConfig);
        }

        return jedisConnectionFactory;
    }

    private static JedisClientConfiguration getJedisClientConfiguration(ZebraCacheConfig zebraCacheConfig, JedisPoolConfig jedisPoolConfig) {
        long timeout = zebraCacheConfig.getTimeout();
        long readTimeout = zebraCacheConfig.getReadTimeout();

        return JedisClientConfiguration.builder()
                    .clientName(CLIENT_NAME)
                    .connectTimeout(Duration.ofMillis(timeout))
                    .readTimeout(Duration.ofMillis(readTimeout))
                    .usePooling()
                    .poolConfig(jedisPoolConfig)
                    .build();
    }

    private static JedisConnectionFactory getJedisConnectionFactory(String host, ZebraCacheConfig zebraCacheConfig, JedisClientConfiguration clientConfig) {
        int port = zebraCacheConfig.getPort();
        int database = zebraCacheConfig.getDatabase();
        String password = zebraCacheConfig.getPassword();

        JedisConnectionFactory jedisConnectionFactory;
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(host, port);
        standaloneConfig.setDatabase(database);
        standaloneConfig.setPassword(RedisPassword.of(password));
        jedisConnectionFactory = new JedisConnectionFactory(standaloneConfig, clientConfig);
        return jedisConnectionFactory;
    }

    private static JedisConnectionFactory getRedisClusterConfiguration(String clusterNodes, int maxRedirects, JedisClientConfiguration clientConfig) {
        JedisConnectionFactory jedisConnectionFactory;
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(
                Arrays.asList(clusterNodes.split(",")));
        clusterConfig.setMaxRedirects(maxRedirects);
        jedisConnectionFactory = new JedisConnectionFactory(clusterConfig, clientConfig);
        return jedisConnectionFactory;
    }
}
