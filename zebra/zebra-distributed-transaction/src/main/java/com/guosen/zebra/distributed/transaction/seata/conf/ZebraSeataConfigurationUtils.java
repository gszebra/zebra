package com.guosen.zebra.distributed.transaction.seata.conf;

import com.google.common.base.Suppliers;
import com.guosen.zebra.core.util.ApplicationContextUtil;
import com.guosen.zebra.distributed.transaction.common.ZebraSeataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * ZebraSeataConfiguration工具类
 */
public final class ZebraSeataConfigurationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZebraSeataConfiguration.class);

    /**
     * Zebra分布式事务配置前缀
     */
    private static final String ZEBRA_CONF_PREFIX = "zebra.distributed.transaction";

    /**
     * Zebra默认的的seata配置
     */
    private static final String DEFAULT_ZEBRA_SEATA_CONF_FILE = "/defaultSeataConf.properties";

    /**
     * vgroup映射前缀
     */
    private static final String SERVICE_VGROUP_MAPPING_PREFIX = "service.vgroup_mapping.";

    /**
     * 默认组名
     */
    private static final String DEFAULT_GROUP_NAME = "default";

    /**
     * ZebraSeataConfiguration实例
     */
    private static final Supplier<ZebraSeataConfiguration> ZEBRA_SEATA_CONFIGURATION_INSTANCE =
            Suppliers.memoize(ZebraSeataConfigurationUtils::get);

    private ZebraSeataConfigurationUtils(){}

    /**
     * 获取ZebraSeataConfiguration
     * @return ZebraSeataConfiguration
     */
    public static ZebraSeataConfiguration getZebraSeataConfiguration() {
        return ZEBRA_SEATA_CONFIGURATION_INSTANCE.get();
    }

    private static ZebraSeataConfiguration get() {
        Properties configCenterProperties = getConfigCenterProperties();

        initDefaultConfiguration(configCenterProperties);
        setGroupProperty(configCenterProperties);

        return new ZebraSeataConfiguration(configCenterProperties);
    }

    private static Properties getConfigCenterProperties() {
        // 将配置中心配置的ZEBRA_CONF_PREFIX前缀配置解析出来
        Environment environment = ApplicationContextUtil.getEnvironment();
        BindResult<Properties> configCenterPropertiesBindResult = Binder.get(environment)
                .bind(ZEBRA_CONF_PREFIX, Properties.class);

        if (!configCenterPropertiesBindResult.isBound()) {
            String errorMessage = "Could not found any configuration of prefix : " + ZEBRA_CONF_PREFIX;
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        return configCenterPropertiesBindResult.get();
    }

    private static void initDefaultConfiguration(Properties configCenterProperties) {
        Properties defaultProperties = getDefaultProperties();

        // 如果配置中心没有配置对应的配置项，那么从默认配置中取出来给加入进去
        Enumeration<?> defaultPropertyNames = defaultProperties.propertyNames();
        while (defaultPropertyNames.hasMoreElements()) {
            String defaultPropertyName = (String) defaultPropertyNames.nextElement();
            if (configCenterProperties.get(defaultPropertyName) == null) {
                Object defaultValue = defaultProperties.get(defaultPropertyName);
                configCenterProperties.put(defaultPropertyName, defaultValue);
            }
        }
    }

    private static Properties getDefaultProperties() {
        // 加载jar包下zebra seata的默认配置
        Properties defaultProperties = new Properties();
        try (InputStream inputStream = ZebraSeataConfigurationUtils.class.getResourceAsStream(DEFAULT_ZEBRA_SEATA_CONF_FILE)) {
            defaultProperties.load(inputStream);
        } catch (IOException e) {
            // 其实不可能发生，除非修改了jar包把该配置文件给删了。
            LOGGER.error("Failed to load zebra seat default properties file : {}", DEFAULT_ZEBRA_SEATA_CONF_FILE, e);
        }

        return defaultProperties;
    }

    private static void setGroupProperty(Properties configCenterProperties) {
        // 设置 service.vgroup_mapping.${微服务seata groupName}=default
        String serviceVGroupMappingKey = SERVICE_VGROUP_MAPPING_PREFIX + ZebraSeataUtils.getTxServiceGroup();
        configCenterProperties.put(serviceVGroupMappingKey, DEFAULT_GROUP_NAME);
    }
}
