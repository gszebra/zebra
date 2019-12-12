package com.guosen.zebra.database.sharding.conf;

import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlTableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlInlineShardingStrategyConfiguration;

import java.util.Map;

/**
 * Zebra Sharding-JDBC 分库分表配置补全器<br/>
 * Sharding-JDBC要求YamlInlineShardingStrategyConfiguration所有字段都配置，无论是默认还是具体的配置。
 * 但是zebra存在具体配置只配置某些项，然后其他使用默认配置的场景，需对这种场景做下特殊处理，对配置进行补全。
 */
public class ZebraShardingDefaultCfgFiller {

    /**
     * 占位表，无实际意义
     */
    private static final String PLACE_HOLDER_TABLE = "THIS_IS_TABLE_PLACE_HOLDER";

    public static void checkThenFill(ZebraShardingDataSourceCfg zebraShardingDataSourceCfg) {
        ZebraShardingRuleConfiguration zebraShardingRuleConfiguration = zebraShardingDataSourceCfg.getSharding();
        YamlShardingStrategyConfiguration defaultTableStrategy = zebraShardingRuleConfiguration.getDefaultTableStrategy();
        if (defaultTableStrategy == null) {
            // 没有配置则直接返回
            return;
        }

        YamlInlineShardingStrategyConfiguration defaultTableInline = defaultTableStrategy.getInline();
        fillDefaultInline(defaultTableInline);

        Map<String, YamlTableRuleConfiguration> tables = zebraShardingDataSourceCfg.getSharding().getTables();
        for (Map.Entry<String, YamlTableRuleConfiguration> tableEntry : tables.entrySet()) {
            YamlTableRuleConfiguration tableConfiguration = tableEntry.getValue();

            // 先支持table.inline默认配置补全
            fillInlineIfNotCfg(tableConfiguration, defaultTableInline);
        }
    }

    private static void fillDefaultInline(YamlInlineShardingStrategyConfiguration defaultTableInline) {
        if (defaultTableInline == null) {
            return;
        }

        // sharding jdbc swap的时候，需要YamlInlineShardingStrategyConfiguration两个字段都不为空，否则会抛异常
        // 但是我们的场景都只是配置sharding-column，所以给它设置一个不存在的表
        if (StringUtils.isBlank(defaultTableInline.getAlgorithmExpression())) {
            defaultTableInline.setAlgorithmExpression(PLACE_HOLDER_TABLE);
        }
    }

    private static void fillInlineIfNotCfg(YamlTableRuleConfiguration tableConfiguration, YamlInlineShardingStrategyConfiguration defaultTableInline) {
        if (defaultTableInline == null) {
            return;
        }

        YamlShardingStrategyConfiguration tableStrategy = tableConfiguration.getTableStrategy();
        YamlInlineShardingStrategyConfiguration tableInline = tableStrategy.getInline();
        if (tableInline == null) {
            // 没有配置inline不做处理，因为有可能是配置了complex等其他类型的
            return;
        }

        // 如果配置了inline，inline的algorithm-expression肯定是不一样的，所以只看sharding-column
        String defaultShardingColumn = defaultTableInline.getShardingColumn();
        if (StringUtils.isBlank(tableInline.getShardingColumn())) {
            tableInline.setShardingColumn(defaultShardingColumn);
        }
    }
}
