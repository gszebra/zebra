package com.guosen.zebra.distributed.transaction.seata.conf;

import io.seata.common.loader.LoadLevel;
import io.seata.config.Configuration;
import io.seata.config.ConfigurationProvider;

/**
 * Zebra Seata配置Provider
 *
 * Zebra所有配置采用自己的配置中心，Seata提供ConfigType.Custom类型让我们扩展配置Provider
 *
 * 在registry.conf指定config.type=Custom
 * 在/META-INF/seata/custom/io.seata.config.ConfigurationProvider文件中指定此SPI
 */
@LoadLevel(name = "Custom")
public class ZebraSeataConfigurationProvider implements ConfigurationProvider {

    @Override
    public Configuration provide() {
        return ZebraSeataConfigurationUtils.getZebraSeataConfiguration();
    }
}
