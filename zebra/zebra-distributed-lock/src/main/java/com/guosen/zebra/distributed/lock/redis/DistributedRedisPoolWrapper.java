package com.guosen.zebra.distributed.lock.redis;

import com.guosen.zebra.distributed.lock.conf.DistributedLockConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * DistributedRedisPool包装器，用于管理RedisPool生命周期
 */
public class DistributedRedisPoolWrapper implements InitializingBean, DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedRedisPoolWrapper.class);

    private DistributedLockConfiguration conf;
    private JedisPool jedisPool;

    public DistributedRedisPoolWrapper(DistributedLockConfiguration conf) {
        this.conf = conf;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    @Override
    public void afterPropertiesSet(){
        String host = conf.getRedisHostName();
        String password = conf.getRedisPassword();
        if (StringUtils.isEmpty(password)) {
            password = null;
        }
        int port = conf.getRedisPort();
        int db = conf.getRedisDb();

        LOGGER.info("Begin to create redis pool for distributed lock, host name : {}, port : {}, db : {}",
                host, port, db);

        jedisPool = new JedisPool(new JedisPoolConfig(), host, port,
                Protocol.DEFAULT_TIMEOUT, password, db);

        LOGGER.info("Finish create redis pool for distributed lock, host name : {}, port : {}, db : {}",
                host, port, db);
    }

    @Override
    public void destroy() {
        LOGGER.info("Begin to close redis pool for distributed lock.");

        if (jedisPool != null) {
            jedisPool.close();
        }

        LOGGER.info("Finish close redis pool for distributed lock.");
    }
}
