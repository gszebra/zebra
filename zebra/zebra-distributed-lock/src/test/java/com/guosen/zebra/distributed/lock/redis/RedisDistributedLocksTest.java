package com.guosen.zebra.distributed.lock.redis;

import com.guosen.zebra.core.util.ApplicationContextUtil;
import com.guosen.zebra.distributed.lock.DistributedLock;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class RedisDistributedLocksTest {
    @Tested
    private RedisDistributedLocks redisDistributedLocks;

    @Mocked
    private ApplicationContextUtil applicationContextUtil;

    @Test
    public void testGetLockEmpty() {
        new Expectations() {
            {
                ApplicationContextUtil.getBean(DistributedRedisPoolWrapper.class);
                result = new NoSuchBeanDefinitionException("DistributedRedisPoolWrapper");
            }
        };

        DistributedLock distributedLock = RedisDistributedLocks.getLock("myLock", 30L);

        assertThat(distributedLock, is(nullValue()));
    }

}
