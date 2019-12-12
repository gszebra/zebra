package com.guosen.zebra.distributed.lock.redis;

import com.guosen.zebra.distributed.lock.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.UUID;

/**
 * Redis实现的分布式锁<br/>
 * 实现参考<a href="https://wudashan.cn/2017/10/23/Redis-Distributed-Lock-Implement/">Redis分布式锁的正确实现方式（Java版）</a>
 */
public class RedisDistributedLock implements DistributedLock {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisDistributedLock.class);

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "EX";
    private static final Long RELEASE_SUCCESS = 1L;
    private static final String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private final JedisPool jedisPool;
    private final String lockName;
    private final long expireTimeSeconds;
    private final String lockValue;

    public RedisDistributedLock(JedisPool jedisPool, String lockName, long expireTimeSeconds) {
        this.jedisPool = jedisPool;
        this.lockName = lockName;
        this.expireTimeSeconds = expireTimeSeconds;
        this.lockValue = UUID.randomUUID().toString();
    }

    @Override
    public boolean tryLock() {
        String result = null;
        try (Jedis jedis = jedisPool.getResource()){
            result = jedis.set(lockName, lockValue, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTimeSeconds);
        }
        catch (Exception e){
            LOGGER.error("Failed to execute set of jedis", e);
        }

        return LOCK_SUCCESS.equals(result);
    }

    @Override
    public boolean unlock() {
        Object result = null;
        try (Jedis jedis = jedisPool.getResource()){
            result = jedis.eval(UNLOCK_SCRIPT, Collections.singletonList(lockName), Collections.singletonList(lockValue));
        }
        catch (Exception e) {
            LOGGER.error("Failed to unlock", e);
        }

        return RELEASE_SUCCESS.equals(result);
    }
}
