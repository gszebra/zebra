package com.guosen.zebra.database.init;

import com.guosen.zebra.core.util.ApplicationContextUtil;
import com.guosen.zebra.database.mybatis.conf.DataSourceCfg;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 普通数据源相关bean初始化类
 */
class StandaloneDbInit {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneDbInit.class);

    /**
     * 初始化Standalone数据源相关的BeanDefinition
     * @param registry  BeanDefinitionRegistry
     * @param standaloneDataSourceCfgList standalone数据源配置列表
     */
    public void init(BeanDefinitionRegistry registry, List<DataSourceCfg> standaloneDataSourceCfgList) {
        if (CollectionUtils.isEmpty(standaloneDataSourceCfgList)) {
            LOGGER.info("There is no standalone data source.");
            return;
        }

        if (LOGGER.isInfoEnabled()) {
            String dataSourceNames = standaloneDataSourceCfgList.stream()
                    .map(DataSourceCfg::getDataSourceName)
                    .collect(Collectors.joining());

            LOGGER.info("Standalone data source name is/are [{}].", dataSourceNames);
            LOGGER.info("Begin to create and register related bean definitions.");
        }

        for (DataSourceCfg dataSourceCfg : standaloneDataSourceCfgList) {
            doInit(registry, dataSourceCfg);
        }

        LOGGER.info("Finish to create and register related bean definitions.");
    }

    private void doInit(BeanDefinitionRegistry registry, DataSourceCfg dataSourceCfg) {
        LOGGER.info("Begin to create and register related bean definitions, data source info : {}.", dataSourceCfg);

        String dataSourceName = dataSourceCfg.getDataSourceName();
        String basePackage = dataSourceCfg.getBasePackage();

        RegistryUtil.buildAndRegisterDataSourceBdf(registry, dataSourceCfg);
        if (StringUtils.isBlank(basePackage)) {
            LOGGER.info("Data source {} basePackage is empty, just register data source definition", dataSourceName);
            return;
        }

        DataSource dataSource = (DataSource) ApplicationContextUtil.getBean(dataSourceName);

        RegistryUtil.buildAndRegisterSqlSessionFactoryBdf(registry, dataSourceName, dataSource);
        RegistryUtil.buildAndRegisterMapperScanBdf(registry, dataSourceName, basePackage);
        RegistryUtil.buildAndRegisterTxBdf(registry, dataSourceName, dataSource);

        LOGGER.info("Finished to create and register related bean definitions, data source name {}.", dataSourceName);
    }

}
