package com.guosen.zebra.database.valiate;

import com.google.common.collect.Sets;
import com.guosen.zebra.database.exception.ZebraBeansException;
import com.guosen.zebra.database.mybatis.conf.DataSourceCfg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sharding-JDBC数据源配置校验器
 */
public final class ShardingDataSourceValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardingDataSourceValidator.class);

    private ShardingDataSourceValidator(){}

    /**
     * 校验
     * @param shardingDataSourceName    Sharding-JDBC数据源名称
     * @param dsNameOfSharding          Sharding-JDBC引用的普通数据源
     * @param referDataSourceCfgList    开发者配置的普通数据源列表
     */
    public static void validate(String shardingDataSourceName, Set<String> dsNameOfSharding,
                                List<DataSourceCfg> referDataSourceCfgList) {

        // 检查引用的数据源是否都配置了
        validateAllReferDsAreConfigured(shardingDataSourceName, dsNameOfSharding, referDataSourceCfgList);
    }


    private static void validateAllReferDsAreConfigured(String shardingDataSourceName, Set<String> dsNameOfSharding,
                                                        List<DataSourceCfg> referDataSourceCfgList) {
        if (referDataSourceCfgList.size() == dsNameOfSharding.size()) {
            return;
        }

        // 数量不同，表示引用的原始数据源缺失了，记录错误信息，并抛出异常。
        Set<String> referDsNameSet = referDataSourceCfgList.stream()
                .map(DataSourceCfg::getDataSourceName)
                .collect(Collectors.toSet());

        String missingDsNameStr = String.join(",", Sets.difference(dsNameOfSharding, referDsNameSet));
        String dsNameOfShardingStr = String.join(",", dsNameOfSharding);

        String errorMessage = String.format("Failed to init related bean definition of sharding data source. "
                        + "sharding data source name : %s, required normal data sources : %s, but missing: %s",
                shardingDataSourceName,
                dsNameOfShardingStr,
                missingDsNameStr);

        LOGGER.error(errorMessage);
        throw new ZebraBeansException(errorMessage);
    }
}
