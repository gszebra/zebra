package com.guosen.zebra.database.api.sharding;

import org.apache.shardingsphere.api.hint.HintManager;

/**
 * Zebra分库分表、读写分离的Hint Manager，用于数据源的强制路由
 */
public final class ZebraHintManager implements AutoCloseable {

    /**
     * sharding-JDBC的HingManager，作为委托
     */
    private HintManager delegate;

    private ZebraHintManager(){}

    /**
     * 新建Zebra Hint Manager实例
     * @return Zebra Hint Manager实例
     */
    public static ZebraHintManager getInstance() {
        ZebraHintManager instance = new ZebraHintManager();
        instance.delegate = HintManager.getInstance();

        return instance;
    }

    /**
     * 在读写分离的模式下，将数据库的操作强制路由到主库
     */
    public void setMasterRouteOnly() {
        delegate.setMasterRouteOnly();
    }

    @Override
    public void close(){
        delegate.close();
    }
}
