package com.guosen.zebra.database.mybatis.spring;

import com.guosen.zebra.database.exception.ZebraBeansException;
import org.apache.commons.lang3.ArrayUtils;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;

/**
 * SqlSessionFactoryBean BeanDefinition工厂类
 */
public final class SqlSessionFactoryBeanBeanDefinitionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlSessionFactoryBeanBeanDefinitionFactory.class);

    private static final String CLASS_PATH_MAPPER_PATTERN = "classpath:*Mapper.xml";

    private static final String ALL_CLASS_PATH_MAPPER_PATTERN = "classpath*:*Mapper.xml";

    private SqlSessionFactoryBeanBeanDefinitionFactory(){}

    /**
     * 构建SqlSessionFactoryBean BeanDefinition
     * @param dataSource 数据源
     * @return SqlSessionFactoryBean BeanDefinition
     */
    public static BeanDefinition create(DataSource dataSource) {
        Resource[] rs = getResources();
        LOGGER.info("Mapper files are : {}", Arrays.toString(rs));

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(SqlSessionFactoryBean.class);
        beanDefinitionBuilder.addPropertyValue("dataSource", dataSource);
        beanDefinitionBuilder.addPropertyValue("mapperLocations", rs);

        return beanDefinitionBuilder.getBeanDefinition();
    }

    private static Resource[] getResources() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] rs;
        try {
            rs = resolver.getResources(CLASS_PATH_MAPPER_PATTERN);
            if(ArrayUtils.isEmpty(rs)){
                LOGGER.info("Mapper not found on classpath，begin search from next path!");
                rs = resolver.getResources(ALL_CLASS_PATH_MAPPER_PATTERN);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to find mapper files", e);
            throw new ZebraBeansException("Failed to find mapper files", e);
        }

        return rs;
    }
}
