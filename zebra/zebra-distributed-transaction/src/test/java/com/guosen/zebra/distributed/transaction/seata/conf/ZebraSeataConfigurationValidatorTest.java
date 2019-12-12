package com.guosen.zebra.distributed.transaction.seata.conf;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class ZebraSeataConfigurationValidatorTest {

    /**
     * 测试必须配置的配置项不存在的情况
     */
    @Test
    public void testRequiredConfMissing() {
        Properties properties = new Properties();
        properties.put("transport.heartbeat", true);

        ZebraSeataConfiguration zebraSeataConfiguration = new ZebraSeataConfiguration(properties);

        try
        {
            ZebraSeataConfigurationValidator.validate(zebraSeataConfiguration);
            fail("Must have exception");
        }
        catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("service.default.grouplist is required."));
        }
    }

    /**
     * 测试必须配置的配置项存在的情况
     */
    @Test
    public void testRequiredConfOccur() {
        Properties properties = new Properties();
        properties.put("service.default.grouplist", "127.0.0.1:8091");

        ZebraSeataConfiguration zebraSeataConfiguration = new ZebraSeataConfiguration(properties);

        ZebraSeataConfigurationValidator.validate(zebraSeataConfiguration);
    }
}
