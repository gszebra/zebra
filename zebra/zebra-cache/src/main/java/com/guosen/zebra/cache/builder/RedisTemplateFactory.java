package com.guosen.zebra.cache.builder;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisTemplate工厂
 */
public final class RedisTemplateFactory {
    private RedisTemplateFactory(){}

    /**
     * 构建RedisTemplate
     * @return RedisTemplate
     */
    public static RedisTemplate<String, Object> create(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // set key serializer
        StringRedisSerializer serializer = new StringRedisSerializer();
        // 设置key序列化类，否则key前面会多了一些乱码
        redisTemplate.setKeySerializer(serializer);
        redisTemplate.setHashKeySerializer(serializer);
        // fastjson serializer
        GenericFastJsonRedisSerializer fastSerializer = new GenericFastJsonRedisSerializer();
        redisTemplate.setValueSerializer(fastSerializer);
        redisTemplate.setHashValueSerializer(fastSerializer);
        // 如果 KeySerializer 或者 ValueSerializer 没有配置，则对应的
        // KeySerializer、ValueSerializer 才使用这个 Serializer
        redisTemplate.setDefaultSerializer(fastSerializer);
        redisTemplate.setConnectionFactory(jedisConnectionFactory);

        return redisTemplate;
    }
}
