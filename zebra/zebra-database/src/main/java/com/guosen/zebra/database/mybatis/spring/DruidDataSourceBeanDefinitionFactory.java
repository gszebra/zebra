package com.guosen.zebra.database.mybatis.spring;

import com.alibaba.druid.pool.DruidDataSource;
import com.guosen.zebra.database.mybatis.conf.DruidDBConfig;
import com.guosen.zebra.database.mybatis.properties.DruidDbProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public final class DruidDataSourceBeanDefinitionFactory {
    private DruidDataSourceBeanDefinitionFactory(){}

    public static BeanDefinition create(String url, String username, String password, String driverClass) {
        DruidDbProperties druidDbProperties = DruidDBConfig.getInstance().getDruidDbProperties();

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(DruidDataSource.class);
        beanDefinitionBuilder.addPropertyValue("url", url);
        beanDefinitionBuilder.addPropertyValue("username", username);
        beanDefinitionBuilder.addPropertyValue("password", password);
        if (StringUtils.isNotEmpty(driverClass)) {
            beanDefinitionBuilder.addPropertyValue("driverClass", driverClass);
        }
        beanDefinitionBuilder.addPropertyValue("initialSize", druidDbProperties.getInitialSize());
        beanDefinitionBuilder.addPropertyValue("minIdle", druidDbProperties.getMinIdle());
        beanDefinitionBuilder.addPropertyValue("maxActive", druidDbProperties.getMaxActive());
        beanDefinitionBuilder.addPropertyValue("maxWait", druidDbProperties.getMaxWait());
        beanDefinitionBuilder.addPropertyValue("timeBetweenEvictionRunsMillis",
                druidDbProperties.getTimeBetweenEvictionRunsMillis());
        beanDefinitionBuilder.addPropertyValue("minEvictableIdleTimeMillis",
                druidDbProperties.getMinEvictableIdleTimeMillis());
        beanDefinitionBuilder.addPropertyValue("validationQuery", druidDbProperties.getValidationQuery());
        beanDefinitionBuilder.addPropertyValue("testWhileIdle", druidDbProperties.isTestWhileIdle());
        beanDefinitionBuilder.addPropertyValue("testOnBorrow", druidDbProperties.isTestOnBorrow());
        beanDefinitionBuilder.addPropertyValue("testOnReturn", druidDbProperties.isTestOnReturn());

        return beanDefinitionBuilder.getBeanDefinition();
    }

}
