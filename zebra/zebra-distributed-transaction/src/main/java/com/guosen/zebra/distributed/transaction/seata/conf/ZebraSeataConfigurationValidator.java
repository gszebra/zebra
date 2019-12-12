package com.guosen.zebra.distributed.transaction.seata.conf;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ZebraSeataConfiugration校验器
 */
public final class ZebraSeataConfigurationValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZebraSeataConfigurationValidator.class);

    /**
     * default group对应的seata server配置key
     */
    private static final String DEFAULT_GROUP_LIST_KEY = "service.default.grouplist";

    /**
     * 必填的配置项
     */
    private static final List<String> REQUIRED_CONFIG_KEYS = Lists.newArrayList(DEFAULT_GROUP_LIST_KEY);

    private ZebraSeataConfigurationValidator(){}

    /**
     * 执行校验
     * @param zebraSeataConfiguration seata配置
     * @throws IllegalArgumentException 校验不通过
     */
    public static void validate(ZebraSeataConfiguration zebraSeataConfiguration) {
        validateRequiredConf(zebraSeataConfiguration);
    }

    private static void validateRequiredConf(ZebraSeataConfiguration zebraSeataConfiguration) {
        for (String requiredConfigKey : REQUIRED_CONFIG_KEYS) {
            String requiredConfigValue = zebraSeataConfiguration.getConfig(requiredConfigKey);
            if (StringUtils.isBlank(requiredConfigValue)) {
                String errorMessage = requiredConfigKey + " is required.";
                LOGGER.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }
}
