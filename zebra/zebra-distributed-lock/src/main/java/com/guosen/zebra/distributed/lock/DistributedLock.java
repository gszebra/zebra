package com.guosen.zebra.distributed.lock;

import com.guosen.zebra.core.api.annotation.ZebraApi;

import java.util.concurrent.TimeUnit;

/**
 * Zebra分布式锁接口
 */
@ZebraApi
public interface DistributedLock {

    /**
     * 尝试加锁
     * @return true : 加锁成功; false : 加锁失败
     */
    boolean tryLock();

    /**
     * 解锁
     */
    boolean unlock();
}
