package com.guosen.zebra.distributed.transaction.init;

import com.guosen.zebra.distributed.transaction.common.ZebraSeataUtils;
import com.guosen.zebra.distributed.transaction.seata.conf.ZebraSeataConfiguration;
import com.guosen.zebra.distributed.transaction.seata.conf.ZebraSeataConfigurationUtils;
import com.guosen.zebra.distributed.transaction.seata.conf.ZebraSeataConfigurationValidator;
import com.guosen.zebra.distributed.transaction.seata.spring.GlobalTransactionScannerBeanDefinitionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Zebra Seata初始化器
 *
 * 引用的类简介使用到了ApplicationContextUtil，必须在这里加上DependsOn
 */
@Component
@DependsOn("applicationContextUtil")
public class ZebraSeataInit implements BeanDefinitionRegistryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZebraSeataInit.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) {
        doInit(beanDefinitionRegistry);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) {
        // do nothing
    }

    private void doInit(BeanDefinitionRegistry beanDefinitionRegistry) {
        printModuleUsageInfo();
        doInitForSeataConfiguration();
        doInitForSeataRelatedBeans(beanDefinitionRegistry);
    }

    private void printModuleUsageInfo() {
        LOGGER.info("Using Zebra distributed transaction module.");
    }

    private void doInitForSeataConfiguration() {
        // 先解析seata配置，如果校验失败，抛出异常，让程序启动失败
        ZebraSeataConfiguration zebraSeataConfiguration = ZebraSeataConfigurationUtils.getZebraSeataConfiguration();
        ZebraSeataConfigurationValidator.validate(zebraSeataConfiguration);
    }

    private void doInitForSeataRelatedBeans(BeanDefinitionRegistry beanDefinitionRegistry) {
        // 构建seata全局事务扫描器bean，这样才能启用seata
        // 扫描器会触发扫描加了seata相关注解(比如@LocalTCC)的类，然后生成代理
        String applicationId = ZebraSeataUtils.getApplicationId();
        String txServiceGroup = ZebraSeataUtils.getTxServiceGroup();

        LOGGER.info("GlobalTransactionScanner applicationId : {}", applicationId);
        LOGGER.info("GlobalTransactionScanner txServiceGroup : {}", txServiceGroup);

        BeanDefinition globalTransactionScannerBeanDefinition =
                GlobalTransactionScannerBeanDefinitionFactory.create(applicationId, txServiceGroup);

        beanDefinitionRegistry.registerBeanDefinition("zebraGlobalTransactionScanner",
                globalTransactionScannerBeanDefinition);
    }
}
