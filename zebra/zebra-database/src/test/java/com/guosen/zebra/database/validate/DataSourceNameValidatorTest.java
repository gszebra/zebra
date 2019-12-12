package com.guosen.zebra.database.validate;

import com.google.common.collect.Lists;
import com.guosen.zebra.database.mybatis.conf.DataSourceCfg;
import com.guosen.zebra.database.sharding.conf.ZebraShardingDataSourceCfg;
import com.guosen.zebra.database.valiate.DataSourceValidator;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public class DataSourceNameValidatorTest {

    @Test
    public void testDataSourceNameDuplicated() {
        DataSourceCfg dataSourceCfg0 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.hello")
                .dataSourceName("ds0")
                .build();
        DataSourceCfg dataSourceCfg1 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.world")
                .dataSourceName("ds1")
                .build();
        DataSourceCfg dataSourceCfg2 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.hello1")
                .dataSourceName("ds2")
                .build();

        // 和dataSourceCfg1数据源名称重复
        DataSourceCfg dataSourceCfg3 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.world1")
                .dataSourceName("ds1")
                .build();

        ZebraShardingDataSourceCfg shardingDsCfg0 = new ZebraShardingDataSourceCfg();
        shardingDsCfg0.setBasePackage("com.guosen.zebra.hello2");
        ZebraShardingDataSourceCfg shardingDsCfg1 = new ZebraShardingDataSourceCfg();
        shardingDsCfg1.setBasePackage("com.guosen.zebra.foo");
        ZebraShardingDataSourceCfg shardingDsCfg2 = new ZebraShardingDataSourceCfg();
        shardingDsCfg2.setBasePackage("com.guosen.zebra.bar");

        List<DataSourceCfg> standaloneDataSourceCfgList = Lists.newArrayList(dataSourceCfg0,
                dataSourceCfg1,
                dataSourceCfg2,
                dataSourceCfg3);
        Map<String, ZebraShardingDataSourceCfg > zebraShardingDataSourceConfigurationMap = new HashMap<>();
        zebraShardingDataSourceConfigurationMap.put("shardingDs0", shardingDsCfg0);
        zebraShardingDataSourceConfigurationMap.put("shardingDs1", shardingDsCfg1);

        // 和dataSourceCfg2重复
        zebraShardingDataSourceConfigurationMap.put("ds2", shardingDsCfg2);

        try {
            DataSourceValidator.validate(standaloneDataSourceCfgList, Collections.emptyList(), zebraShardingDataSourceConfigurationMap);
            fail("Must have exception");
        }
        catch (IllegalArgumentException e) {
            String expectedErrorMessage = "Found duplicated data source names, ds2 : 2;ds1 : 2;";
            assertThat(e.getMessage(), is(expectedErrorMessage));
        }
    }

    @Test
    public void testDataSourceNameNotDuplicated() {
        DataSourceCfg dataSourceCfg0 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.hello")
                .dataSourceName("ds0")
                .build();
        DataSourceCfg dataSourceCfg1 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.world")
                .dataSourceName("ds1")
                .build();
        DataSourceCfg dataSourceCfg2 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.hello1")
                .dataSourceName("ds2")
                .build();

        DataSourceCfg dataSourceCfg3 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.world1")
                .dataSourceName("ds3")
                .build();

        ZebraShardingDataSourceCfg shardingDsCfg0 = new ZebraShardingDataSourceCfg();
        shardingDsCfg0.setBasePackage("com.guosen.zebra.hello2");
        ZebraShardingDataSourceCfg shardingDsCfg1 = new ZebraShardingDataSourceCfg();
        shardingDsCfg1.setBasePackage("com.guosen.zebra.foo");
        ZebraShardingDataSourceCfg shardingDsCfg2 = new ZebraShardingDataSourceCfg();
        shardingDsCfg2.setBasePackage("com.guosen.zebra.bar");

        List<DataSourceCfg> standaloneDataSourceCfgList = Lists.newArrayList(dataSourceCfg0,
                dataSourceCfg1,
                dataSourceCfg2,
                dataSourceCfg3);
        Map<String, ZebraShardingDataSourceCfg > zebraShardingDataSourceConfigurationMap = new HashMap<>();
        zebraShardingDataSourceConfigurationMap.put("shardingDs0", shardingDsCfg0);
        zebraShardingDataSourceConfigurationMap.put("shardingDs1", shardingDsCfg1);
        zebraShardingDataSourceConfigurationMap.put("shardingDs2", shardingDsCfg2);

        DataSourceValidator.validate(standaloneDataSourceCfgList, Collections.emptyList(), zebraShardingDataSourceConfigurationMap);
    }
}
