package com.guosen.zebra.database.init;

import com.guosen.zebra.database.mybatis.conf.DataSourceCfg;
import com.guosen.zebra.database.mybatis.spring.DataSourceTransactionManagerBeanDefinitionFactory;
import com.guosen.zebra.database.mybatis.spring.DruidDataSourceBeanDefinitionFactory;
import com.guosen.zebra.database.mybatis.spring.MapperScannerConfigurerBeanDefinitionFactory;
import com.guosen.zebra.database.mybatis.spring.SqlSessionFactoryBeanBeanDefinitionFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import javax.sql.DataSource;

/**
 * 数据源相关的Spring Registry工具类
 */
public final class RegistryUtil {

    private RegistryUtil(){}

    /**
     * 注入MapperScan
     */
    public static void buildAndRegisterMapperScanBdf(BeanDefinitionRegistry registry,
                                               String dataSourceName,
                                               String basePackage) {
        String sqlSessionFactoryBeanName = "SqlSessionFactory" + dataSourceName;
        BeanDefinition beanDefinition =
                MapperScannerConfigurerBeanDefinitionFactory.create(sqlSessionFactoryBeanName, basePackage);

        String mapperScannerConfigurerBeanName = "MapperScannerConfigurer" + dataSourceName;
        registry.registerBeanDefinition(mapperScannerConfigurerBeanName, beanDefinition);
    }

    /**
     * 注入TX
     */
    public static void buildAndRegisterTxBdf(BeanDefinitionRegistry registry,
                                       String dataSourceName,
                                       DataSource dataSource) {

        String txBeanName = "TransactionManager" + dataSourceName;
        BeanDefinition beanDefinition = DataSourceTransactionManagerBeanDefinitionFactory.create(dataSource);
        registry.registerBeanDefinition(txBeanName, beanDefinition);
    }

    /**
     * 构造并且注册DataSource的BeanDefinition
     */
    public static void buildAndRegisterDataSourceBdf(BeanDefinitionRegistry registry,
                                               DataSourceCfg dataSourceCfg) {
        String url = dataSourceCfg.getUrl();
        String dataSourceName = dataSourceCfg.getDataSourceName();
        String userName = dataSourceCfg.getUserName();
        String password = dataSourceCfg.getPassword();
        String driverClass = dataSourceCfg.getDriverClass();

        BeanDefinition beanDefinition = DruidDataSourceBeanDefinitionFactory.create(url, userName, password, driverClass);
        registry.registerBeanDefinition(dataSourceName, beanDefinition);
    }

    public static void buildAndRegisterSqlSessionFactoryBdf(BeanDefinitionRegistry registry,
                                                      String dataSourceName,
                                                      DataSource dataSource) {

        BeanDefinition beanDefinition = SqlSessionFactoryBeanBeanDefinitionFactory.create(dataSource);
        registry.registerBeanDefinition("SqlSessionFactory" + dataSourceName,
                beanDefinition);
    }
}
