package com.guosen.zebra.cache.builder;

import com.guosen.zebra.cache.ZebraCacheConfig;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * RedisMessageListenerContainer工厂
 */
public final class RedisMessageListenerContainerFactory {
    private RedisMessageListenerContainerFactory(){}

    /**
     * 创建RedisMessageListenerContainer
     * @return RedisMessageListenerContainer
     */
    public static RedisMessageListenerContainer create(ZebraCacheConfig zebraCacheConfig,
                                                       JedisConnectionFactory jedisConnectionFactory,
                                                       MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(jedisConnectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(zebraCacheConfig.getTopicName()));

        return container;
    }
}
