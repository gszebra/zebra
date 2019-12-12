package com.guosen.zebra.distributed.transaction.seata.spring;

import io.seata.spring.annotation.GlobalTransactionScanner;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * GlobalTransactionScanner Spring BeanDefinition工厂类
 */
public final class GlobalTransactionScannerBeanDefinitionFactory {
    private GlobalTransactionScannerBeanDefinitionFactory(){}

    /**
     * 构建GlobalTransactionScanner Spring BeanDefinition
     * @param applicationId     应用ID
     * @param txServiceGroup    组名
     * @return GlobalTransactionScanner Spring BeanDefinition
     */
    public static BeanDefinition create(String applicationId, String txServiceGroup) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(GlobalTransactionScanner.class);

        beanDefinitionBuilder.addConstructorArgValue(applicationId);
        beanDefinitionBuilder.addConstructorArgValue(txServiceGroup);

        return beanDefinitionBuilder.getBeanDefinition();
    }
}
