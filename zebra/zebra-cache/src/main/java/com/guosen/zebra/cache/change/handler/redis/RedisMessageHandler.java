package com.guosen.zebra.cache.change.handler.redis;

import com.guosen.zebra.cache.LocalRedisCacheManager;
import com.guosen.zebra.cache.change.UpdateMessage;
import com.guosen.zebra.cache.change.handler.common.FirstCacheHandler;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisMessageHandler {
    private RedisTemplate<String, Object> redisTemplate;
    private FirstCacheHandler firstCacheHandler;

    public RedisMessageHandler(RedisTemplate<String, Object> redisTemplate, LocalRedisCacheManager cacheManager) {
        this.redisTemplate = redisTemplate;
        firstCacheHandler = new FirstCacheHandler(cacheManager);
    }

    public void handle(Message message) {
        byte[] body = message.getBody();
        UpdateMessage updateMessage = (UpdateMessage) redisTemplate.getValueSerializer().deserialize(body);
        if (updateMessage == null) {
            return;
        }

        firstCacheHandler.handle(updateMessage);
    }
}
