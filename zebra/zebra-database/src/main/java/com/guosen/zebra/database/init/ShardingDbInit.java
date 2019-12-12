package com.guosen.zebra.database.init;

import com.guosen.zebra.core.util.ApplicationContextUtil;
import com.guosen.zebra.core.opentracing.adapter.ZebraOpenTracingTracer;
import com.guosen.zebra.database.mybatis.conf.DataSourceCfg;
import com.guosen.zebra.database.sharding.conf.ZebraShardingDataSourceCfg;
import com.guosen.zebra.database.sharding.spring.MasterSlaveDataSourceBeanDefinitionFactory;
import com.guosen.zebra.database.sharding.spring.ShardingDataSourceBeanDefinitionFactory;
import com.guosen.zebra.database.valiate.ShardingDataSourceValidator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.opentracing.ShardingTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分库分表、读写分离相关Bean初始化类
 */
class ShardingDbInit {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardingDbInit.class);

    public void init(BeanDefinitionRegistry registry, List<DataSourceCfg> dataSourceCfgList,
              Map<String, ZebraShardingDataSourceCfg> zebraShardingDataSourceConfigurationMap) {

        if (MapUtils.isEmpty(zebraShardingDataSourceConfigurationMap)) {
            LOGGER.info("There is no sharding configuration.");
            return;
        }

        String dataSourceNames = String.join(",", zebraShardingDataSourceConfigurationMap.keySet());
        LOGGER.info("Sharding data source name is/are [{}].", dataSourceNames);
        LOGGER.info("Begin to create and register related bean definitions of sharding data sources.");

        // 初始化相关的数据源及相关bean
        initDataSources(registry, dataSourceCfgList, zebraShardingDataSourceConfigurationMap);

        // 初始化ShardingTracer，以便上报到APM系统
        ShardingTracer.init(ZebraOpenTracingTracer.getInstance());

        LOGGER.info("Finish to create and register related bean definitions of sharding data sources.");
    }

    private void initDataSources(BeanDefinitionRegistry registry, List<DataSourceCfg> dataSourceCfgList, Map<String, ZebraShardingDataSourceCfg> zebraShardingDataSourceConfigurationMap) {
        // 初始化sharding数据源
        for (Map.Entry<String, ZebraShardingDataSourceCfg> entry : zebraShardingDataSourceConfigurationMap.entrySet()) {
            String shardingDataSourceName = entry.getKey();
            ZebraShardingDataSourceCfg zebraShardingDataSourceCfg = entry.getValue();

            doInit(registry, dataSourceCfgList, shardingDataSourceName, zebraShardingDataSourceCfg);
        }
    }

    private void doInit(BeanDefinitionRegistry registry, List<DataSourceCfg> dataSourceCfgList,
                        String shardingDataSourceName, ZebraShardingDataSourceCfg zebraShardingDataSourceCfg) {

        LOGGER.info("Begin to create and register related bean definitions of sharding data source : {}, type : {}",
                shardingDataSourceName, zebraShardingDataSourceCfg.getType());

        Set<String> referDsNameOfSharding = getReferDataSourceNames(zebraShardingDataSourceCfg);

        // 获取sharding数据源相关的普通数据源列表配置
        List<DataSourceCfg> referDataSourceCfgList = dataSourceCfgList.stream()
                .filter(dataSourceCfg -> referDsNameOfSharding.contains(dataSourceCfg.getDataSourceName()))
                .collect(Collectors.toList());

        ShardingDataSourceValidator.validate(shardingDataSourceName, referDsNameOfSharding, referDataSourceCfgList);

        initDatabaseBeans(registry,
                shardingDataSourceName,
                zebraShardingDataSourceCfg,
                referDataSourceCfgList);

        LOGGER.info("Finish to create and register related bean definitions of sharding data source : {}", shardingDataSourceName);
    }

    private Set<String> getReferDataSourceNames(ZebraShardingDataSourceCfg zebraShardingDataSourceCfg) {
        Set<String> referDataSourceNames = new HashSet<>();
        ZebraShardingDataSourceCfg.Type type = zebraShardingDataSourceCfg.getType();
        if (type == ZebraShardingDataSourceCfg.Type.SHARDING) {
            referDataSourceNames = zebraShardingDataSourceCfg.getDatasource().getNames();
        }
        else if (type == ZebraShardingDataSourceCfg.Type.MASTER_SLAVE) {
            String masterDsName = zebraShardingDataSourceCfg.getMasterslave().getMasterDataSourceName();
            Collection<String> slaveDsNames = zebraShardingDataSourceCfg.getMasterslave().getSlaveDataSourceNames();

            if (StringUtils.isNotBlank(masterDsName)) {
                referDataSourceNames.add(masterDsName);
            }
            if (CollectionUtils.isNotEmpty(slaveDsNames)) {
                referDataSourceNames.addAll(slaveDsNames);
            }
        }
        else {
            // do nothing
        }

        return referDataSourceNames;
    }

    private void initDatabaseBeans(BeanDefinitionRegistry registry,
                                   String shardingDataSourceName,
                                   ZebraShardingDataSourceCfg zebraShardingDataSourceCfg,
                                   List<DataSourceCfg> referDataSourceCfgList) {

        // 先注册依赖的普通数据源
        Map<String, DataSource> dataSourceMap = initReferDataSources(registry, referDataSourceCfgList);

        // 构造并注册ShardingSphere的数据源
        BeanDefinition shardingSphereDataSourceDdf;
        if (ZebraShardingDataSourceCfg.Type.SHARDING == zebraShardingDataSourceCfg.getType()) {
            shardingSphereDataSourceDdf = ShardingDataSourceBeanDefinitionFactory.getBeanDefinition(dataSourceMap,
                    zebraShardingDataSourceCfg);
        }
        else {
            shardingSphereDataSourceDdf = MasterSlaveDataSourceBeanDefinitionFactory.getBeanDefinition(dataSourceMap,
                    zebraShardingDataSourceCfg);
        }

        registry.registerBeanDefinition(shardingDataSourceName, shardingSphereDataSourceDdf);

        String basePackage = zebraShardingDataSourceCfg.getBasePackage();
        if (StringUtils.isBlank(basePackage)) {
            // 在使用IA框架的情况下，是不用配置basePackage的，IA的Mapper文件是固定的
            return;
        }

        DataSource shardingDataSource = (DataSource) ApplicationContextUtil.getBean(shardingDataSourceName);

        RegistryUtil.buildAndRegisterSqlSessionFactoryBdf(registry, shardingDataSourceName, shardingDataSource);
        RegistryUtil.buildAndRegisterMapperScanBdf(registry, shardingDataSourceName, basePackage);
        RegistryUtil.buildAndRegisterTxBdf(registry, shardingDataSourceName, shardingDataSource);
    }

    private Map<String, DataSource> initReferDataSources(BeanDefinitionRegistry registry,
                                                         List<DataSourceCfg> referDataSourceCfgList) {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        for (DataSourceCfg dataSourceCfg : referDataSourceCfgList) {
            LOGGER.info("Create and register refer data source bean, {}", dataSourceCfg);
            RegistryUtil.buildAndRegisterDataSourceBdf(registry, dataSourceCfg);

            DataSource dataSource = (DataSource) ApplicationContextUtil.getBean(dataSourceCfg.getDataSourceName());
            dataSourceMap.put(dataSourceCfg.getDataSourceName(), dataSource);
        }

        return dataSourceMap;
    }
}
