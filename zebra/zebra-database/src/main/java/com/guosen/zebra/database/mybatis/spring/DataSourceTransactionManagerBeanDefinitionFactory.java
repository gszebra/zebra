package com.guosen.zebra.database.mybatis.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

public final class DataSourceTransactionManagerBeanDefinitionFactory {

    private DataSourceTransactionManagerBeanDefinitionFactory(){}

    public static BeanDefinition create(DataSource dataSource) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(DataSourceTransactionManager.class);
        beanDefinitionBuilder.addConstructorArgValue(dataSource);
        return beanDefinitionBuilder.getBeanDefinition();
    }
}
