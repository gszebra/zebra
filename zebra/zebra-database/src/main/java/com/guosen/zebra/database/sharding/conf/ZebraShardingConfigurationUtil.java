package com.guosen.zebra.database.sharding.conf;

import com.guosen.zebra.core.util.ApplicationContextUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingStrategyConfiguration;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * ZebraShardingConfiguration工具类
 */
public final class ZebraShardingConfigurationUtil {
    /**
     * zebra sharding配置在spring properties中的前缀
     */
    private static final String ZEBRA_SHARDING_CONF_PREFIX = "zebra.database";

    private ZebraShardingConfigurationUtil(){}

    /**
     * 获取ZebraShardingConfiguration
     */
    public static ZebraShardingConfiguration getZebraShardingConfiguration() {

        /*
         * 使用Spring Boot的Binder方式来读取，而不是常用的@ConfigurationProperties来自动装配。
         * 是因为ZebraShardingConfiguration会在实现了BeanDefinitionRegistryPostProcessor的类中使用
         * 此时@ConfigurationProperties注解的类还未被处理，对应的值皆为空。
         * 所以程序必须用Binder方式显式读取。
         */
        Environment environment = ApplicationContextUtil.getEnvironment();
        BindResult<ZebraShardingConfiguration> bindResult = Binder.get(environment)
                        .bind(ZEBRA_SHARDING_CONF_PREFIX, Bindable.of(ZebraShardingConfiguration.class));

        ZebraShardingConfiguration cfg = bindResult.isBound() ? bindResult.get() : null;

        if (cfg != null) {
            setType(cfg);
            setNameIfMasterSlave(cfg);
            fillDefaultCfgOfShardingIfNotCfg(cfg);
        }

        return cfg;
    }


    private static void setType(ZebraShardingConfiguration cfg) {
        Map<String, ZebraShardingDataSourceCfg> cfgMap = cfg.getShardingcfg();
        if (MapUtils.isEmpty(cfgMap)) {
            return;
        }

        for (ZebraShardingDataSourceCfg zebraShardingDataSourceCfg : cfgMap.values()) {
            setType(zebraShardingDataSourceCfg);
        }
    }

    private static void setType(ZebraShardingDataSourceCfg zebraShardingDataSourceCfg) {
        ZebraShardingDataSourceCfg.Type type = ZebraShardingDataSourceCfg.Type.SHARDING;
        if (zebraShardingDataSourceCfg.getMasterslave() != null) {
            type = ZebraShardingDataSourceCfg.Type.MASTER_SLAVE;
        }

        zebraShardingDataSourceCfg.setType(type);
    }

    private static void setNameIfMasterSlave(ZebraShardingConfiguration cfg) {
        Map<String, ZebraShardingDataSourceCfg> cfgMap = cfg.getShardingcfg();
        if (MapUtils.isEmpty(cfgMap)) {
            return;
        }

        /*
         * ShardingSphere中,读写分离数据源名称在配置spring.shardingsphere.masterslave.name中配置
         * Zebra对配置项做了定制，读写分离数据源名称修改为在zebra.database.shardingcfg.${读写分离数据源名称}中配置
         * 但是ShardingSphere的相关代码会读取spring.shardingsphere.masterslave.name配置，如果为空，会报错。
         * 所以对读写分离的配置项做下特殊处理，将zebra的${读写分离数据源名称}赋值给spring.shardingsphere.masterslave.name
         */
        for (Map.Entry<String, ZebraShardingDataSourceCfg> cfgEntry : cfgMap.entrySet()) {
            String cfgDataSourceName = cfgEntry.getKey();
            ZebraShardingDataSourceCfg zebraShardingDataSourceCfg = cfgEntry.getValue();
            if (zebraShardingDataSourceCfg.getType() == ZebraShardingDataSourceCfg.Type.MASTER_SLAVE) {
                zebraShardingDataSourceCfg.getMasterslave().setName(cfgDataSourceName);
            }
        }
    }

    private static void fillDefaultCfgOfShardingIfNotCfg(ZebraShardingConfiguration cfg) {
        Map<String, ZebraShardingDataSourceCfg> cfgMap = cfg.getShardingcfg();
        if (MapUtils.isEmpty(cfgMap)) {
            return;
        }

        for (ZebraShardingDataSourceCfg zebraShardingDataSourceCfg : cfgMap.values()) {
            if (zebraShardingDataSourceCfg.getType() != ZebraShardingDataSourceCfg.Type.SHARDING) {
                continue;
            }

            ZebraShardingDefaultCfgFiller.checkThenFill(zebraShardingDataSourceCfg);
        }
    }

}
