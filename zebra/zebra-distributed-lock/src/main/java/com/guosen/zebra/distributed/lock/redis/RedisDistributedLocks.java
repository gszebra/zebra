package com.guosen.zebra.distributed.lock.redis;

import com.guosen.zebra.core.util.ApplicationContextUtil;
import com.guosen.zebra.distributed.lock.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import redis.clients.jedis.JedisPool;

public class RedisDistributedLocks {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisDistributedLocks.class);

    /**
     * 获取分布式锁对象（只适用微服务的不同实例，不同微服务之间不适用）
     * @param lockName  锁名称
     * @param lockExpiredTimeSeconds 持有锁的超时时间（单位：秒），最好设置大于业务执行时间。
     * @return  分布式锁对象，如果获取失败，则返回null
     */
    public static DistributedLock getLock(String lockName, long lockExpiredTimeSeconds) {
        JedisPool jedisPool = getJedisPool();
        if (jedisPool == null) {
            LOGGER.error("Failed to get jedis pool.");
            return null;
        }

        return new RedisDistributedLock(jedisPool, lockName, lockExpiredTimeSeconds);
    }

    private static JedisPool getJedisPool() {
        DistributedRedisPoolWrapper distributedRedisPoolWrapper = null;
        try {
            distributedRedisPoolWrapper = ApplicationContextUtil.getBean(DistributedRedisPoolWrapper.class);
        }
        catch (BeansException e) {
            LOGGER.error("Failed to get bean of {}", DistributedRedisPoolWrapper.class, e);
        }

        if (distributedRedisPoolWrapper == null) {
            return null;
        }

        return distributedRedisPoolWrapper.getJedisPool();
    }
}
