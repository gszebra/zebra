package com.guosen.zebra.database.init;

import com.guosen.zebra.database.mybatis.conf.DataSourceCfgUtil;
import com.guosen.zebra.database.sharding.conf.ZebraShardingConfigurationUtil;
import com.guosen.zebra.database.utils.OperateProperties;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DatabaseInitTest {
    @Tested
    private DatabaseInit databaseInit;

    @Mocked
    private OperateProperties operateProperties;

    @Mocked
    private BeanDefinitionRegistry registry;

    /**
     * 测试配置文件找不到的情况
     */
    @Test
    public void testPropertiesNotFound() throws IOException {
        new Expectations() {
            {
                OperateProperties.loadProperties(anyString);
                result = new IOException("file not found");
            }
        };

        try {
            databaseInit.postProcessBeanDefinitionRegistry(registry);
            fail("Must have exception");
        }
        catch (BeansException e) {
            assertThat(e.getMessage(), is("Failed to load properties file, file name : localCache; nested exception is java.io.IOException: file not found"));
        }
    }

    /**
     * 测试没有配置数据源的情况
     */
    @Test
    public void testNoDataSourceCfg(@Mocked DataSourceCfgUtil dataSourceCfgUtil,
                                    @Mocked ZebraShardingConfigurationUtil zebraShardingConfigurationUtil) {
        new Expectations() {
            {
                DataSourceCfgUtil.getDataSourceCfgList();
                result = Collections.emptyList();
            }
        };

        databaseInit.postProcessBeanDefinitionRegistry(registry);

        new Verifications() {
            {
                ZebraShardingConfigurationUtil.getZebraShardingConfiguration();
                times = 0;
            }
        };
    }
}
