package com.guosen.zebra.cache.change.listener;

import com.guosen.zebra.cache.change.handler.redis.RedisMessageHandler;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * Redis消息队列机制的缓存变更消息监听器，适用于大部分场景
 */
public class CacheChangeRedisListener implements MessageListener {

    private RedisMessageHandler redisMessageHandler;

    public CacheChangeRedisListener(RedisMessageHandler redisMessageHandler) {
        this.redisMessageHandler = redisMessageHandler;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        redisMessageHandler.handle(message);
    }
}
