package com.guosen.zebra.cache.change.publisher;

import com.guosen.zebra.cache.change.UpdateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis方式缓存变更消息发布器
 */
public class CacheChangeRedisPublisher implements CacheChangePublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheChangeRedisPublisher.class);
    private String topicName;
    private RedisTemplate<String, Object> redisTemplate;

    public CacheChangeRedisPublisher(String topicName, RedisTemplate<String, Object> redisTemplate) {
        this.topicName = topicName;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void publish(UpdateMessage message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Begin to public message {} to topic {}", message.toString(), topicName);
        }

        redisTemplate.convertAndSend(topicName, message);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Finish to public message {} to topic {}", message.toString(), topicName);
        }
    }
}
