package com.guosen.zebra.distributed.lock.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

// 先预留zebra.distributed.lock.type，以便后续有新的分布式实现扩展
@Configuration
@ConditionalOnProperty(name = "zebra.distributed.lock.type", havingValue = "redis")
public class DistributedLockConfiguration {

    /**
     * redis host name
     */
    @Value("${zebra.distributed.lock.redis.host}")
    private String redisHostName;

    /**
     * redis端口
     */
    @Value("${zebra.distributed.lock.redis.port:6379}")
    private int redisPort;

    /**
     * redis数据库index
     */
    @Value("${zebra.distributed.lock.redis.db:0}")
    private int redisDb;

    /**
     * redis密码
     */
    @Value("${zebra.distributed.lock.redis.password:}")
    private String redisPassword;

    public String getRedisHostName() {
        return redisHostName;
    }

    public void setRedisHostName(String redisHostName) {
        this.redisHostName = redisHostName;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }

    public int getRedisDb() {
        return redisDb;
    }

    public void setRedisDb(int redisDb) {
        this.redisDb = redisDb;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }
}
