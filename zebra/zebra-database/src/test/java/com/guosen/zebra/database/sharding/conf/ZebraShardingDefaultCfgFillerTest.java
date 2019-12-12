package com.guosen.zebra.database.sharding.conf;

import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlTableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlInlineShardingStrategyConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

public class ZebraShardingDefaultCfgFillerTest {

    /**
     * 测试表策略，inline : 部分默认配置 + 部分具体配置的情况
     */
    @Test
    public void test1() {

        // 默认配置
        YamlInlineShardingStrategyConfiguration defaultInline = new YamlInlineShardingStrategyConfiguration();
        defaultInline.setShardingColumn("default_column");
        YamlShardingStrategyConfiguration defaultTableStrategy = new YamlShardingStrategyConfiguration();
        defaultTableStrategy.setInline(defaultInline);

        // table1配置
        YamlInlineShardingStrategyConfiguration inlineOfTable1 = new YamlInlineShardingStrategyConfiguration();
        inlineOfTable1.setAlgorithmExpression("t_table1->${default_column}");

        YamlShardingStrategyConfiguration tableStrategyOfTable1 = new YamlShardingStrategyConfiguration();
        tableStrategyOfTable1.setInline(inlineOfTable1);

        YamlTableRuleConfiguration table1 = new YamlTableRuleConfiguration();
        table1.setTableStrategy(tableStrategyOfTable1);

        // 设置tables
        Map<String, YamlTableRuleConfiguration> tables = new HashMap<>();
        tables.put("shardingDs01", table1);

        ZebraShardingRuleConfiguration sharding = new ZebraShardingRuleConfiguration();
        sharding.setDefaultTableStrategy(defaultTableStrategy);
        sharding.setTables(tables);
        ZebraShardingDataSourceCfg zebraShardingDataSourceCfg = new ZebraShardingDataSourceCfg();
        zebraShardingDataSourceCfg.setSharding(sharding);

        ZebraShardingDefaultCfgFiller.checkThenFill(zebraShardingDataSourceCfg);

        // 检查
        String defaultTableExpression = zebraShardingDataSourceCfg
                .getSharding()
                .getDefaultTableStrategy()
                .getInline()
                .getAlgorithmExpression();

        // 默认表 expression 自动补充
        assertThat(defaultTableExpression, is("THIS_IS_TABLE_PLACE_HOLDER"));

        Collection<YamlTableRuleConfiguration> newTables = zebraShardingDataSourceCfg
                .getSharding()
                .getTables()
                .values();
        YamlTableRuleConfiguration newTable1 = newTables.iterator().next();
        YamlInlineShardingStrategyConfiguration newInlineOfTable1 = newTable1.getTableStrategy().getInline();

        // 分表字段设置为默认值
        assertThat(newInlineOfTable1.getShardingColumn(), is("default_column"));

        // 分表表达式还是原来的
        assertThat(newInlineOfTable1.getAlgorithmExpression(), is("t_table1->${default_column}"));
    }


    /**
     * 测试表策略，inline : 全部默认配置 + 全部具体配置的情况
     */
    @Test
    public void test2() {
        // 默认配置
        YamlInlineShardingStrategyConfiguration defaultInline = new YamlInlineShardingStrategyConfiguration();
        defaultInline.setShardingColumn("default_column");
        defaultInline.setAlgorithmExpression("t_fake_table->${default_column}");
        YamlShardingStrategyConfiguration defaultTableStrategy = new YamlShardingStrategyConfiguration();
        defaultTableStrategy.setInline(defaultInline);

        // table1配置
        YamlInlineShardingStrategyConfiguration inlineOfTable1 = new YamlInlineShardingStrategyConfiguration();
        inlineOfTable1.setAlgorithmExpression("t_table1->${my_column}");
        inlineOfTable1.setShardingColumn("my_column");

        YamlShardingStrategyConfiguration tableStrategyOfTable1 = new YamlShardingStrategyConfiguration();
        tableStrategyOfTable1.setInline(inlineOfTable1);

        YamlTableRuleConfiguration table1 = new YamlTableRuleConfiguration();
        table1.setTableStrategy(tableStrategyOfTable1);

        // 设置tables
        Map<String, YamlTableRuleConfiguration> tables = new HashMap<>();
        tables.put("shardingDs01", table1);

        ZebraShardingRuleConfiguration sharding = new ZebraShardingRuleConfiguration();
        sharding.setDefaultTableStrategy(defaultTableStrategy);
        sharding.setTables(tables);
        ZebraShardingDataSourceCfg zebraShardingDataSourceCfg = new ZebraShardingDataSourceCfg();
        zebraShardingDataSourceCfg.setSharding(sharding);

        ZebraShardingDefaultCfgFiller.checkThenFill(zebraShardingDataSourceCfg);

        // 检查
        String defaultTableExpression = zebraShardingDataSourceCfg
                .getSharding()
                .getDefaultTableStrategy()
                .getInline()
                .getAlgorithmExpression();

        // 默认表 expression 设置了就不自动补充
        assertThat(defaultTableExpression, is("t_fake_table->${default_column}"));

        Collection<YamlTableRuleConfiguration> newTables = zebraShardingDataSourceCfg
                .getSharding()
                .getTables()
                .values();
        YamlTableRuleConfiguration newTable1 = newTables.iterator().next();
        YamlInlineShardingStrategyConfiguration newInlineOfTable1 = newTable1.getTableStrategy().getInline();

        // 分表字段设置就不设置为默认值
        assertThat(newInlineOfTable1.getShardingColumn(), is("my_column"));

        // 分表表达式还是原来的
        assertThat(newInlineOfTable1.getAlgorithmExpression(), is("t_table1->${my_column}"));
    }
}
