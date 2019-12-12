package com.guosen.zebra.distributed.lock;

import com.guosen.zebra.core.api.annotation.ZebraApi;
import com.guosen.zebra.core.util.ServiceNameUtil;
import com.guosen.zebra.distributed.lock.redis.RedisDistributedLocks;

/**
 * Zebra分布式锁工具类
 */
@ZebraApi
public class DistributedLocks {

    /**
     * 获取分布式锁对象（只适用微服务的不同实例，不同微服务之间不适用）
     * @param lockName  锁名称
     * @param lockExpiredTimeSeconds 持有锁的超时时间（单位：秒），最好设置大于业务执行时间。
     * @return  分布式锁对象，如果获取失败，则返回null
     */
    public static DistributedLock getLock(String lockName, long lockExpiredTimeSeconds) {
        String realRockName = buildRealLockName(lockName);
        return RedisDistributedLocks.getLock(realRockName, lockExpiredTimeSeconds);
    }

    private static String buildRealLockName(String lockName) {
        // 每个锁加上微服务的全称，确保不同微服务之间不会冲突。
        String serviceName = ServiceNameUtil.getServiceName();
        return "ZebraDistributedLock::" + serviceName + "::" + lockName;
    }
}
