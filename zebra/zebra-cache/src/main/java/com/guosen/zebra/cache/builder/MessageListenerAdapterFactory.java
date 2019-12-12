package com.guosen.zebra.cache.builder;

import com.guosen.zebra.cache.LocalRedisCacheManager;
import com.guosen.zebra.cache.change.handler.redis.RedisMessageHandler;
import com.guosen.zebra.cache.change.listener.CacheChangeRedisListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * MessageListenerAdapter工厂
 */
public final class MessageListenerAdapterFactory {
    private MessageListenerAdapterFactory(){}

    /**
     * 构建MessageListenerAdapter
     * @return MessageListenerAdapter
     */
    public static MessageListenerAdapter create(LocalRedisCacheManager cacheManager,
                                                RedisTemplate<String, Object> redisTemplate) {
        RedisMessageHandler redisMessageHandler = new RedisMessageHandler(redisTemplate, cacheManager);
        return new MessageListenerAdapter(new CacheChangeRedisListener(redisMessageHandler));
    }
}
