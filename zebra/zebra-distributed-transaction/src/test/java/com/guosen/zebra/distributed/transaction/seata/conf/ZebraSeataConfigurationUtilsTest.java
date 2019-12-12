package com.guosen.zebra.distributed.transaction.seata.conf;

import com.guosen.zebra.core.util.ApplicationContextUtil;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.Test;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ZebraSeataConfigurationUtilsTest {

    @Tested
    private ZebraSeataConfigurationUtils zebraSeataConfigurationUtils;

    @Mocked
    private ApplicationContextUtil applicationContextUtil;

    @Mocked
    private Environment environment;

    @Mocked
    private Binder binder;

    @Mocked
    private BindResult bindResult;

    /**
     * 执行测试
     *
     * 由于是单例的缘故，只能测试正常情况
     */
    @Test
    public void test() {

        final Properties configCenterProperties = new Properties();
        configCenterProperties.put("service.default.grouplist", "127.0.0.1:8091");
        configCenterProperties.put("transport.thread-factory.worker-thread-size", 7);
        configCenterProperties.put("client.tm.rollback.retry.count", 2);

        new Expectations() {
            {
                ApplicationContextUtil.getEnvironment();
                result = environment;
            }
            {
                Binder.get(environment);
                result = binder;
            }
            {
                binder.bind(anyString, Properties.class);
                result = bindResult;
            }
            {
                bindResult.isBound();
                result = true;
            }
            {
                bindResult.get();
                result = configCenterProperties;
            }
        };

        ZebraSeataConfiguration zebraSeataConfiguration =
                ZebraSeataConfigurationUtils.getZebraSeataConfiguration();

        // 不在默认配置的
        assertThat(zebraSeataConfiguration.getConfig("service.default.grouplist"), is("127.0.0.1:8091"));

        // 在默认配置有，但是配置中心配置的了
        assertThat(zebraSeataConfiguration.getInt("transport.thread-factory.worker-thread-size"), is(7));
        assertThat(zebraSeataConfiguration.getInt("client.tm.rollback.retry.count"), is(2));

        // 默认配置
        assertThat(zebraSeataConfiguration.getInt("transaction.undo.log.delete.period"), is(86400000));
    }
}
