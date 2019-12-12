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

public class CommonDbInit {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonDbInit.class);

    /**
     * 初始化Common数据源相关的BeanDefinition
     * @param registry  BeanDefinitionRegistry
     * @param commonDataSourceCfgList standalone数据源配置列表
     */
    public void init(BeanDefinitionRegistry registry, List<DataSourceCfg> commonDataSourceCfgList) {
        if (CollectionUtils.isEmpty(commonDataSourceCfgList)) {
            LOGGER.info("There is no common data source.");
            return;
        }

        List<DataSourceCfg> newCommonDataSourceCfgList = generateNewCfgs(commonDataSourceCfgList);

        if (LOGGER.isInfoEnabled()) {
            String dataSourceNames = newCommonDataSourceCfgList.stream()
                    .map(DataSourceCfg::getDataSourceName)
                    .collect(Collectors.joining());

            LOGGER.info("Common data source name is/are [{}].", dataSourceNames);
            LOGGER.info("Begin to create and register related bean definitions.");
        }

        for (DataSourceCfg dataSourceCfg : newCommonDataSourceCfgList) {
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

    private List<DataSourceCfg> generateNewCfgs(List<DataSourceCfg> commonDataSourceCfgList) {
        return commonDataSourceCfgList.stream()
                .map(this::generateNewCfg)
                .collect(Collectors.toList());
    }

    private DataSourceCfg generateNewCfg(DataSourceCfg oldCfg) {

        String newDataSourceName = generateNewDataSourceName(oldCfg.getDataSourceName());

        // 必须new一个新的，oldCfg在sharding那里还会用到
        DataSourceCfg newCfg = new DataSourceCfg();
        newCfg.setBasePackage(oldCfg.getBasePackage());
        newCfg.setDataSourceName(newDataSourceName);
        newCfg.setDriverClass(oldCfg.getDriverClass());
        newCfg.setUrl(oldCfg.getUrl());
        newCfg.setUserName(oldCfg.getUserName());
        newCfg.setPassword(oldCfg.getPassword());

        return newCfg;
    }

    private String generateNewDataSourceName(String oldDataSourceName) {

        // 构建新数据源名称，
        // 在sharding中的对应的数据源还是用户配置为什么就是什么，因为分库规则会使用到
        // 单独的basePackage中，数据源名称给加上ZebraCommon前缀，确保bean名称不重复

        return "ZebraCommon" + StringUtils.upperCase(oldDataSourceName.substring(0, 1))
                + oldDataSourceName.substring(1);
    }
}
