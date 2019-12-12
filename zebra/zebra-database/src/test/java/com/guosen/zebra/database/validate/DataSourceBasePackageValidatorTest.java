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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

public class DataSourceBasePackageValidatorTest {

    /**
     * 测试basePackage没有重复的情况
     */
    @Test
    public void testBasePackageNoDuplicated() {
        DataSourceCfg dataSourceCfg0 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.hello")
                .dataSourceName("ds0")
                .build();
        DataSourceCfg dataSourceCfg1 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.world")
                .dataSourceName("ds1")
                .build();

        DataSourceCfg dataSourceCfg3 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.nihao")
                .dataSourceName("ds2")
                .build();

        ZebraShardingDataSourceCfg shardingDsCfg0 = new ZebraShardingDataSourceCfg();
        shardingDsCfg0.setBasePackage("com.guosen.zebra.ping");
        ZebraShardingDataSourceCfg shardingDsCfg1 = new ZebraShardingDataSourceCfg();
        shardingDsCfg1.setBasePackage("com.guosen.zebra.foo");
        ZebraShardingDataSourceCfg shardingDsCfg2 = new ZebraShardingDataSourceCfg();
        shardingDsCfg2.setBasePackage("com.guosen.zebra.bar");

        List<DataSourceCfg> standaloneDataSourceCfgList = Lists.newArrayList(dataSourceCfg0, dataSourceCfg1);

        List<DataSourceCfg> commonDataSourceCfgList = Lists.newArrayList(dataSourceCfg3);

        Map<String, ZebraShardingDataSourceCfg > zebraShardingDataSourceConfigurationMap = new HashMap<>();
        zebraShardingDataSourceConfigurationMap.put("shardingDs0", shardingDsCfg0);
        zebraShardingDataSourceConfigurationMap.put("shardingDs1", shardingDsCfg1);
        zebraShardingDataSourceConfigurationMap.put("shardingDs2", shardingDsCfg2);

        DataSourceValidator.validate(standaloneDataSourceCfgList, commonDataSourceCfgList, zebraShardingDataSourceConfigurationMap);
    }

    /**
     * 测试只有standalone数据源basePackage重复的情况
     */
    @Test
    public void testBasePackageDuplicatedOnlyStandalone() {
        DataSourceCfg dataSourceCfg0 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.hello")
                .dataSourceName("ds0")
                .build();
        DataSourceCfg dataSourceCfg1 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.hello")
                .dataSourceName("ds1")
                .build();
        DataSourceCfg dataSourceCfg2 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.world")
                .dataSourceName("ds2")
                .build();

        DataSourceCfg dataSourceCfg3 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.hello")
                .dataSourceName("ds3")
                .build();

        List<DataSourceCfg> standaloneDataSourceCfgList = Lists.newArrayList(dataSourceCfg0, dataSourceCfg1, dataSourceCfg2);
        List<DataSourceCfg> commonDataSourceCfgList = Lists.newArrayList(dataSourceCfg3);

        Map<String, ZebraShardingDataSourceCfg > zebraShardingDataSourceConfigurationMap = new HashMap<>();

        try {
            DataSourceValidator.validate(standaloneDataSourceCfgList, commonDataSourceCfgList, zebraShardingDataSourceConfigurationMap);
            fail("Must have exception");
        }
        catch (IllegalArgumentException e) {
            String expectedErrorMessage = "Found duplicated basePackage config in data source, {com.guosen.zebra.hello : [ds0,ds1,ds3]}";
            assertThat(e.getMessage(), equalTo(expectedErrorMessage));
        }
    }

    /**
     * 测试一个数据源配置多个basePackage但是与其他数据源的basePackage重复的情况
     */
    @Test
    public void testBasePackageMultiDuplicatedOnlyStandalone() {
        DataSourceCfg dataSourceCfg0 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.hello,com.guosen.zebra.world")
                .dataSourceName("ds0")
                .build();
        DataSourceCfg dataSourceCfg1 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.world")
                .dataSourceName("ds1")
                .build();
        DataSourceCfg dataSourceCfg2 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.foo")
                .dataSourceName("ds2")
                .build();

        List<DataSourceCfg> standaloneDataSourceCfgList = Lists.newArrayList(dataSourceCfg0, dataSourceCfg1, dataSourceCfg2);
        Map<String, ZebraShardingDataSourceCfg > zebraShardingDataSourceConfigurationMap = new HashMap<>();

        try {
            DataSourceValidator.validate(standaloneDataSourceCfgList, Collections.emptyList(), zebraShardingDataSourceConfigurationMap);
            fail("Must have exception");
        }
        catch (IllegalArgumentException e) {
            String expectedErrorMessage = "Found duplicated basePackage config in data source, {com.guosen.zebra.world : [ds0,ds1]}";
            assertThat(e.getMessage(), equalTo(expectedErrorMessage));
        }
    }

    /**
     * 测试只有sharding数据源basePackage重复的情况
     */
    @Test
    public void testBasePackageDuplicatedOnlySharding() {
        DataSourceCfg dataSourceCfg0 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.hello")
                .dataSourceName("ds0")
                .build();
        DataSourceCfg dataSourceCfg1 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.world")
                .dataSourceName("ds1")
                .build();

        ZebraShardingDataSourceCfg shardingDsCfg0 = new ZebraShardingDataSourceCfg();
        shardingDsCfg0.setBasePackage("com.guosen.zebra.foo");
        ZebraShardingDataSourceCfg shardingDsCfg1 = new ZebraShardingDataSourceCfg();
        shardingDsCfg1.setBasePackage("com.guosen.zebra.foo");
        ZebraShardingDataSourceCfg shardingDsCfg2 = new ZebraShardingDataSourceCfg();
        shardingDsCfg2.setBasePackage("com.guosen.zebra.bar");

        List<DataSourceCfg> standaloneDataSourceCfgList = Lists.newArrayList(dataSourceCfg0, dataSourceCfg1);
        Map<String, ZebraShardingDataSourceCfg > zebraShardingDataSourceConfigurationMap = new HashMap<>();
        zebraShardingDataSourceConfigurationMap.put("shardingDs0", shardingDsCfg0);
        zebraShardingDataSourceConfigurationMap.put("shardingDs1", shardingDsCfg1);
        zebraShardingDataSourceConfigurationMap.put("shardingDs2", shardingDsCfg2);

        try {

            DataSourceValidator.validate(standaloneDataSourceCfgList, Collections.emptyList(), zebraShardingDataSourceConfigurationMap);
            fail("Must have exception");
        }
        catch (IllegalArgumentException e) {
            String expectedErrorMessage = "Found duplicated basePackage config in data source, {com.guosen.zebra.foo : [shardingDs0,shardingDs1]}";
            assertThat(e.getMessage(), equalTo(expectedErrorMessage));
        }
    }

    /**
     * 测试standalone和sharding数据源basePackage重复的情况
     */
    @Test
    public void testBasePackageDuplicatedStandaloneAndSharding() {
        DataSourceCfg dataSourceCfg0 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.hello")
                .dataSourceName("ds0")
                .build();
        DataSourceCfg dataSourceCfg1 = DataSourceCfg.Builder.newBuilder()
                .basePackage("com.guosen.zebra.world")
                .dataSourceName("ds1")
                .build();

        ZebraShardingDataSourceCfg shardingDsCfg0 = new ZebraShardingDataSourceCfg();
        shardingDsCfg0.setBasePackage("com.guosen.zebra.hello");
        ZebraShardingDataSourceCfg shardingDsCfg1 = new ZebraShardingDataSourceCfg();
        shardingDsCfg1.setBasePackage("com.guosen.zebra.foo");
        ZebraShardingDataSourceCfg shardingDsCfg2 = new ZebraShardingDataSourceCfg();
        shardingDsCfg2.setBasePackage("com.guosen.zebra.bar");

        List<DataSourceCfg> standaloneDataSourceCfgList = Lists.newArrayList(dataSourceCfg0, dataSourceCfg1);
        Map<String, ZebraShardingDataSourceCfg > zebraShardingDataSourceConfigurationMap = new HashMap<>();
        zebraShardingDataSourceConfigurationMap.put("shardingDs0", shardingDsCfg0);
        zebraShardingDataSourceConfigurationMap.put("shardingDs1", shardingDsCfg1);
        zebraShardingDataSourceConfigurationMap.put("shardingDs2", shardingDsCfg2);

        try {
            DataSourceValidator.validate(standaloneDataSourceCfgList, Collections.emptyList(), zebraShardingDataSourceConfigurationMap);
            fail("Must have exception");
        }
        catch (IllegalArgumentException e) {
            String expectedErrorMessage = "Found duplicated basePackage config in data source, {com.guosen.zebra.hello : [ds0,shardingDs0]}";
            assertThat(e.getMessage(), equalTo(expectedErrorMessage));
        }
    }
}
