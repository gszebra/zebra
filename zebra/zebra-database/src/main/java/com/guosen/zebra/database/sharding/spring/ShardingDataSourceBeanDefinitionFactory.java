package com.guosen.zebra.database.sharding.spring;

import com.guosen.zebra.database.sharding.conf.ZebraShardingDataSourceCfg;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * ShardingDataSource SpringBeanDefinition工厂类
 */
public final class ShardingDataSourceBeanDefinitionFactory {

    private ShardingDataSourceBeanDefinitionFactory(){}

    /**
     * 构建ShardingDataSource SpringBeanDefinition
     * @see org.apache.shardingsphere.shardingjdbc.api.yaml.YamlShardingDataSourceFactory
     */
    public static BeanDefinition getBeanDefinition(Map<String, DataSource> dataSourceMap,
                                                   ZebraShardingDataSourceCfg zebraShardingDataSourceCfg) {

        ShardingRuleConfiguration shardingRuleConfiguration =
                new ShardingRuleConfigurationYamlSwapper().swap(zebraShardingDataSourceCfg.getSharding());
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfiguration, dataSourceMap.keySet());
        Properties props = zebraShardingDataSourceCfg.getProps();

        return getBeanDefinition(dataSourceMap, shardingRule, props);
    }

    private static BeanDefinition getBeanDefinition(Map<String, DataSource> dataSourceMap,
                                                    ShardingRule shardingRule,
                                                    Properties props) {

        // 对应new ShardingDataSource(dataSourceMap, shardingRule, props)
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(ShardingDataSource.class);
        beanDefinitionBuilder.addConstructorArgValue(dataSourceMap);
        beanDefinitionBuilder.addConstructorArgValue(shardingRule);
        beanDefinitionBuilder.addConstructorArgValue(props);

        return beanDefinitionBuilder.getBeanDefinition();
    }
}
