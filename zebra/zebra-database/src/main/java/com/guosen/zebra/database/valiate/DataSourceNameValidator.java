package com.guosen.zebra.database.valiate;

import com.guosen.zebra.database.mybatis.conf.DataSourceCfg;
import com.guosen.zebra.database.sharding.conf.ZebraShardingDataSourceCfg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源名称校验器
 */
public class DataSourceNameValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceNameValidator.class);

    /**
     * 校验数据源配置
     */
    public static void validate(List<DataSourceCfg> standaloneDataSourceCfgList,
                                List<DataSourceCfg> commonDataSourceCfgList,
                                Map<String, ZebraShardingDataSourceCfg> zebraShardingDataSourceConfigurationMap) {
        validateDataSourceNameDuplicated(standaloneDataSourceCfgList, commonDataSourceCfgList, zebraShardingDataSourceConfigurationMap);
    }

    /**
     * 校验是否重复配置了数据源名称
     */
    private static void validateDataSourceNameDuplicated(List<DataSourceCfg> standaloneDataSourceCfgList,
                                                         List<DataSourceCfg> commonDataSourceCfgList,
                                                         Map<String, ZebraShardingDataSourceCfg> zebraShardingDataSourceConfigurationMap) {
        Map<String, Integer> dataSourceNameCount = new HashMap<>();
        for (DataSourceCfg dataSourceCfg : standaloneDataSourceCfgList) {
            String dataSourceName = dataSourceCfg.getDataSourceName();
            refreshDataSourceCountMap(dataSourceName, dataSourceNameCount);
        }

        for (DataSourceCfg dataSourceCfg : commonDataSourceCfgList) {
            String dataSourceName = dataSourceCfg.getDataSourceName();
            refreshDataSourceCountMap(dataSourceName, dataSourceNameCount);
        }

        for (String shardingDataSourceName : zebraShardingDataSourceConfigurationMap.keySet()) {
            refreshDataSourceCountMap(shardingDataSourceName, dataSourceNameCount);
        }

        StringBuilder duplicatedDataSourceMessage = new StringBuilder();
        for (Map.Entry<String, Integer> dataSourceNameCountEntry : dataSourceNameCount.entrySet()) {
            String dataSourceName = dataSourceNameCountEntry.getKey();
            int count = dataSourceNameCountEntry.getValue();
            if (count > 1) {
                duplicatedDataSourceMessage.append(dataSourceName).append(" : ").append(count).append(';');
            }
        }

        if (duplicatedDataSourceMessage.length() == 0) {
            return;
        }

        String errorMessage = "Found duplicated data source names, " + duplicatedDataSourceMessage.toString();
        LOGGER.error(errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    private static void refreshDataSourceCountMap(String dataSourceName, Map<String, Integer> dataSourceNameCount) {
        if (dataSourceNameCount.containsKey(dataSourceName)) {
            int cn = dataSourceNameCount.get(dataSourceName);
            dataSourceNameCount.put(dataSourceName, ++cn);
        }
        else {
            dataSourceNameCount.put(dataSourceName, 1);
        }
    }
}
