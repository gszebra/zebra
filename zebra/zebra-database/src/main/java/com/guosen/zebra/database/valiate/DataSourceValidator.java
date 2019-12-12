package com.guosen.zebra.database.valiate;

import com.guosen.zebra.database.mybatis.conf.DataSourceCfg;
import com.guosen.zebra.database.sharding.conf.ZebraShardingDataSourceCfg;

import java.util.List;
import java.util.Map;

/**
 * 通用数据源校验器
 */
public final class DataSourceValidator {

    private DataSourceValidator(){}

    /**
     * 校验数据源的配置
     * @param standaloneDataSourceCfgList   独立数据源配置
     * @param commonDataSourceCfgList       公共数据源配置
     * @param zebraShardingDataSourceConfigurationMap   Sharding-JDBC数据源配置
     * @throws IllegalArgumentException 配置存在问题
     */
    public static void validate(List<DataSourceCfg> standaloneDataSourceCfgList,
                                List<DataSourceCfg> commonDataSourceCfgList,
                                Map<String, ZebraShardingDataSourceCfg> zebraShardingDataSourceConfigurationMap) {
        DataSourceBasePackageValidator.validate(standaloneDataSourceCfgList, commonDataSourceCfgList, zebraShardingDataSourceConfigurationMap);
        DataSourceNameValidator.validate(standaloneDataSourceCfgList, commonDataSourceCfgList, zebraShardingDataSourceConfigurationMap);
    }
}
