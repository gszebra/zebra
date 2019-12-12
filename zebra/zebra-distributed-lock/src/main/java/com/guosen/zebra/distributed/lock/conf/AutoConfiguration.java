package com.guosen.zebra.distributed.lock.conf;


import com.guosen.zebra.distributed.lock.redis.DistributedRedisPoolWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AutoConfiguration {

    @Bean
    @ConditionalOnBean(DistributedLockConfiguration.class)
    public DistributedRedisPoolWrapper distributedRedisPoolWrapper(DistributedLockConfiguration distributedLockConfiguration) {
        return new DistributedRedisPoolWrapper(distributedLockConfiguration);
    }
}
