package com.guosen.zebra.database.valiate;

import com.guosen.zebra.database.mybatis.conf.DataSourceCfg;
import com.guosen.zebra.database.sharding.conf.ZebraShardingDataSourceCfg;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 数据源的BasePackage校验
 */
public class DataSourceBasePackageValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceBasePackageValidator.class);

    /**
     * 校验basePackage配置
     */
    public static void validate(List<DataSourceCfg> standaloneDataSourceCfgList,
                                List<DataSourceCfg> commonDataSourceCfgList,
                                Map<String, ZebraShardingDataSourceCfg> zebraShardingDataSourceConfigurationMap) {
        validateBasePackageDuplicated(standaloneDataSourceCfgList, commonDataSourceCfgList, zebraShardingDataSourceConfigurationMap);
    }

    /**
     * 校验basePackage是否重复配置
     */
    private static void validateBasePackageDuplicated(List<DataSourceCfg> standaloneDataSourceCfgList,
                                                      List<DataSourceCfg> commonDataSourceCfgList,
                                                      Map<String, ZebraShardingDataSourceCfg> zebraShardingDataSourceConfigurationMap) {

        // 获取basePackage对应的数据源集合，做此额外操作是为了告诉开发那个basePackage在哪些数据源重复配置了
        // 以便开发者快速定位
        Map<String, Set<String>> basePackageDataSourcesMap = getBasePackageDataSourcesMap(standaloneDataSourceCfgList,
                commonDataSourceCfgList, zebraShardingDataSourceConfigurationMap);

        StringBuilder errorMessageBuilder = new StringBuilder("Found duplicated basePackage config in data source, {");

        boolean duplicated = false;
        for (Map.Entry<String, Set<String>> entry : basePackageDataSourcesMap.entrySet()) {
            String basePackage = entry.getKey();
            if (StringUtils.isBlank(basePackage)) {
                continue;
            }

            Set<String> relatedDataSources = entry.getValue();

            if (relatedDataSources.size() > 1) {
                duplicated = true;
                String relatedDataSourcesStr = String.join(",", entry.getValue());
                errorMessageBuilder.append(basePackage)
                        .append(" : [")
                        .append(relatedDataSourcesStr)
                        .append("],");
            }
        }

        if (!duplicated) {
            return;
        }

        errorMessageBuilder.deleteCharAt(errorMessageBuilder.length() - 1);
        errorMessageBuilder.append('}');

        String errorMessage = errorMessageBuilder.toString();
        LOGGER.error(errorMessage);

        throw new IllegalArgumentException(errorMessage);
    }

    private static Map<String, Set<String>> getBasePackageDataSourcesMap(List<DataSourceCfg> standaloneDataSourceCfgList,
                                                                         List<DataSourceCfg> commonDataSourceCfgList,
                                                                         Map<String, ZebraShardingDataSourceCfg> zebraShardingDataSourceConfigurationMap) {
        Map<String, Set<String>> basePackageDataSourcesMap = new HashMap<>();
        for (DataSourceCfg dataSourceCfg : standaloneDataSourceCfgList) {
            String basePackage = dataSourceCfg.getBasePackage();
            String dataSourceName = dataSourceCfg.getDataSourceName();
            if (StringUtils.isBlank(basePackage)) {
                continue;
            }

            refreshMap(basePackageDataSourcesMap, basePackage, dataSourceName);
        }

        for (DataSourceCfg dataSourceCfg : commonDataSourceCfgList) {
            String basePackage = dataSourceCfg.getBasePackage();
            String dataSourceName = dataSourceCfg.getDataSourceName();

            // common的basePackage肯定不为空

            refreshMap(basePackageDataSourcesMap, basePackage, dataSourceName);
        }

        for (Map.Entry<String, ZebraShardingDataSourceCfg> entry : zebraShardingDataSourceConfigurationMap.entrySet()) {
            String basePackage = entry.getValue().getBasePackage();
            String dataSourceName = entry.getKey();
            if (StringUtils.isBlank(basePackage)) {
                continue;
            }

            refreshMap(basePackageDataSourcesMap, basePackage, dataSourceName);
        }

        return basePackageDataSourcesMap;
    }

    private static void refreshMap(Map<String, Set<String>> backPackageDataSourcesMap, String basePackageCfg, String dataSourceName) {

        // 一个数据源可以支持多个basePackage，它们用逗号隔开
        String[] basePackages = StringUtils.split(basePackageCfg, ",");

        for (String basePackage : basePackages) {
            backPackageDataSourcesMap.computeIfAbsent(basePackage, k -> new HashSet<>())
                    .add(dataSourceName);
        }
    }
}
