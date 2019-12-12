package com.guosen.zebra.distributed.transaction.seata.conf;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ZebraSeataConfigurationTest {

    @Test
    public void testGet() {
        Properties properties = new Properties();
        properties.put("transport.heartbeat", true);
        properties.put("service.default.grouplist", "127.0.0.1:8091");
        properties.put("transport.shutdown.wait", 3);

        ZebraSeataConfiguration zebraSeataConfiguration = new ZebraSeataConfiguration(properties);

        boolean transportHeartbeat = zebraSeataConfiguration.getBoolean("transport.heartbeat");
        assertThat(transportHeartbeat, is(true));

        String serviceDefaultGroupList = zebraSeataConfiguration.getConfig("service.default.grouplist");
        assertThat(serviceDefaultGroupList, is("127.0.0.1:8091"));

        int transportShutdownWait = zebraSeataConfiguration.getInt("transport.shutdown.wait");
        assertThat(transportShutdownWait, is(3));

        String configOfDefaultValue = zebraSeataConfiguration.getConfig("hello.world", "ping.pong");
        assertThat(configOfDefaultValue, is("ping.pong"));


        String notExistConf = zebraSeataConfiguration.getConfig("not.exist");
        assertThat(notExistConf, is(nullValue()));
    }
}
