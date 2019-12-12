package com.guosen.zebra.database.validate;


import com.google.common.collect.Sets;
import com.guosen.zebra.database.exception.ZebraBeansException;
import com.guosen.zebra.database.mybatis.conf.DataSourceCfg;
import com.guosen.zebra.database.valiate.ShardingDataSourceValidator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ShardingCfgValidatorTest {

    /**
     * 测试引用的数据源缺失的情况
     */
    @Test
    public void testReferDataSourceMissing() {
        Set<String> dsNameOfSharding = Sets.newHashSet("ds0", "ds1");

        DataSourceCfg dataSourceCfg = new DataSourceCfg();
        dataSourceCfg.setDataSourceName("ds0");
        List<DataSourceCfg> referDataSourceCfgList = new ArrayList<>();
        referDataSourceCfgList.add(dataSourceCfg);

        String shardingDataSourceName = "shardingDs0";
        try {
            ShardingDataSourceValidator.validate(shardingDataSourceName, dsNameOfSharding, referDataSourceCfgList);
            fail("Must have exception");
        }
        catch (ZebraBeansException e) {
            String expectedErrorMessage = "Failed to init related bean definition of sharding data source. " +
                    "sharding data source name : shardingDs0, required normal data sources : ds0,ds1, but missing: ds1";
            assertThat(e.getMessage(), is(expectedErrorMessage));
        }
    }

    /**
     * 测试引用的数据源匹配的情况（没有缺失）
     */
    @Test
    public void testReferDataSourceMatch() {
        String shardingDataSourceName = "shardingDs0";
        Set<String> dsNameOfSharding = Sets.newHashSet("ds0", "ds1");

        DataSourceCfg dataSourceCfg0 = new DataSourceCfg();
        dataSourceCfg0.setDataSourceName("ds0");
        DataSourceCfg dataSourceCfg1 = new DataSourceCfg();
        dataSourceCfg1.setDataSourceName("ds1");

        List<DataSourceCfg> referDataSourceCfgList = new ArrayList<>();
        referDataSourceCfgList.add(dataSourceCfg0);
        referDataSourceCfgList.add(dataSourceCfg1);

        ShardingDataSourceValidator.validate(shardingDataSourceName, dsNameOfSharding, referDataSourceCfgList);
    }
}
