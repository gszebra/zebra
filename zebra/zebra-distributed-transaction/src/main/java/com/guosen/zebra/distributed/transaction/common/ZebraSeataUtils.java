package com.guosen.zebra.distributed.transaction.common;

import com.guosen.zebra.core.registry.etcd.EtcdRegistry;

/**
 * Zebra Seata工具类
 */
public final class ZebraSeataUtils {

    private ZebraSeataUtils(){}

    /**
     * 获取应用ID
     * @return 应用ID
     */
    public static String getApplicationId() {
        return EtcdRegistry.getServiceName();
    }

    /**
     * 获取组名
     * @return 组名
     */
    public static String getTxServiceGroup() {
        return getApplicationId() + "-group";
    }
}
