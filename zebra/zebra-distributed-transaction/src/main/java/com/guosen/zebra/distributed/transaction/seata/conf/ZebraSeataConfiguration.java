package com.guosen.zebra.distributed.transaction.seata.conf;

import io.seata.config.AbstractConfiguration;
import io.seata.config.ConfigType;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Zebra Seata配置类，从Zebra配置中心获取数据
 */
public class ZebraSeataConfiguration extends AbstractConfiguration {
    /**
     * 保存配置中心seata对应的配置Properties对象
     */
    private final Properties seataConfiguration;

    public ZebraSeataConfiguration(Properties seataConfiguration) {
        this.seataConfiguration = seataConfiguration;
    }

    @Override
    public String getTypeName() {
        return ConfigType.Custom.name();
    }

    @Override
    public String getConfig(String dataId, String defaultValue, long timeoutMills) {
        // 忽略timeoutMills参数，zebra seata的配置都在seataConfiguration对象中了
        // 此处不要直接转换为String或者String.valueOf，因为可能要返回null或者取到的对象不是String类型
        Object value = seataConfiguration.getOrDefault(dataId, defaultValue);
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public boolean putConfig(String dataId, String content, long timeoutMills) {
        // 不支持此操作，直接返回成功即可
        return true;
    }

    @Override
    public boolean putConfigIfAbsent(String dataId, String content, long timeoutMills) {
        // 不支持此操作，直接返回成功即可
        return true;
    }

    @Override
    public boolean removeConfig(String dataId, long timeoutMills) {
        // 不支持此操作，直接返回成功即可
        return true;
    }

    @Override
    public void addConfigListener(String dataId, Object listener) {
        // do nothing
    }

    @Override
    public void removeConfigListener(String dataId, Object listener) {
        // do nothing
    }

    @Override
    public List getConfigListeners(String dataId) {
        // 不支持此操作，直接返回空列表即可
        return Collections.emptyList();
    }
}
