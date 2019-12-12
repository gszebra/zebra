package com.guosen.zebra.database.sharding.spring;

import com.guosen.zebra.database.sharding.conf.ZebraMasterSlaveRuleConfiguration;
import com.guosen.zebra.database.sharding.conf.ZebraShardingDataSourceCfg;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * 读写分离数据源SpringBeanDefinition工厂类
 */
public final class MasterSlaveDataSourceBeanDefinitionFactory {
    private MasterSlaveDataSourceBeanDefinitionFactory(){}

    /**
     * 构建MasterSlaveDataSource SpringBeanDefinition
     * @see org.apache.shardingsphere.shardingjdbc.api.yaml.YamlMasterSlaveDataSourceFactory
     */
    public static BeanDefinition getBeanDefinition(Map<String, DataSource> dataSourceMap,
                                                   ZebraShardingDataSourceCfg zebraShardingDataSourceCfg) {

        ZebraMasterSlaveRuleConfiguration zebraMasterSlaveRuleConfiguration = zebraShardingDataSourceCfg.getMasterslave();
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration =
                new MasterSlaveRuleConfigurationYamlSwapper().swap(zebraMasterSlaveRuleConfiguration);
        MasterSlaveRule masterSlaveRule = new MasterSlaveRule(masterSlaveRuleConfiguration);
        Properties props = zebraShardingDataSourceCfg.getProps();

        return getBeanDefinition(dataSourceMap, masterSlaveRule, props);
    }

    private static BeanDefinition getBeanDefinition(Map<String, DataSource> dataSourceMap,
                                                    MasterSlaveRule masterSlaveRule,
                                                    Properties props) {

        // 对应new MasterSlaveDataSource(dataSourceMap, shardingRule, props)
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(MasterSlaveDataSource.class);
        beanDefinitionBuilder.addConstructorArgValue(dataSourceMap);
        beanDefinitionBuilder.addConstructorArgValue(masterSlaveRule);
        beanDefinitionBuilder.addConstructorArgValue(props);

        return beanDefinitionBuilder.getBeanDefinition();
    }
}
