package com.guosen.zebra.database.mybatis.spring;

import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * MapperScannerConfigurer BeanDefinition工厂类
 */
public final class MapperScannerConfigurerBeanDefinitionFactory {

    private MapperScannerConfigurerBeanDefinitionFactory(){}

    /**
     * 构建MapperScannerConfigurer BeanDefinition
     * @param sqlSessionFactoryBeanName
     * @param basePackage MyBatis Mapper basePackage
     * @return MapperScannerConfigurer BeanDefinition
     */
    public static BeanDefinition create(String sqlSessionFactoryBeanName, String basePackage) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(MapperScannerConfigurer.class);
        beanDefinitionBuilder.addPropertyValue("sqlSessionFactoryBeanName", sqlSessionFactoryBeanName);
        beanDefinitionBuilder.addPropertyValue("basePackage", basePackage);
        return beanDefinitionBuilder.getBeanDefinition();
    }
}
