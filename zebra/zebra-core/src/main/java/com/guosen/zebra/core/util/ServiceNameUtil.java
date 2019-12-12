package com.guosen.zebra.core.util;

import com.google.common.base.Suppliers;
import com.guosen.zebra.core.boot.runner.RpcSeriviceBaseInfoReader;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;
import com.guosen.zebra.core.serializer.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

/**
 * Zebra微服务名称工具类
 */
public final class ServiceNameUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceNameUtil.class);

    private static final Supplier<String> SERVICE_NAME_SUPPLIER = Suppliers.memoize(ServiceNameUtil::doGetServiceName);

    /**
     * 获取当前微服务全称
     * @return 当前微服务全称
     */
    public static String getServiceName() {
        return SERVICE_NAME_SUPPLIER.get();
    }

    private static String doGetServiceName() {
        RpcSeriviceBaseInfoReader rpcSeriviceBaseInfoReader = ApplicationContextUtil.getBean(RpcSeriviceBaseInfoReader.class);

        List<RpcServiceBaseInfo> rpcSeriviceBaseInfos = rpcSeriviceBaseInfoReader.getRpcSeriviceBaseInfos();
        if (CollectionUtils.isEmpty(rpcSeriviceBaseInfos)) {
            LOGGER.error("Could not get service name.");
            throw new IllegalArgumentException("Could not get service name.");
        }
        
        return rpcSeriviceBaseInfos.get(0).getService();
    }
}
