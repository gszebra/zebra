package com.guosen.zebra.database.sharding.conf;

import com.guosen.zebra.core.util.ApplicationContextUtil;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.Test;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.nullValue;

public class ZebraShardingConfigurationUtilTest {

    @Tested
    private ZebraShardingConfigurationUtil zebraShardingConfigurationUtil;

    @Mocked
    private ApplicationContextUtil applicationContextUtil;

    @Mocked
    private Environment environment;

    @Mocked
    private Binder binder;

    @Mocked
    BindResult<ZebraShardingConfiguration> bindResult;

    @Test
    public void testNoConfig() {
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
                @SuppressWarnings("unchecked")
                Bindable<ZebraShardingConfiguration> bindable = (Bindable<ZebraShardingConfiguration>) any;
                binder.bind("zebra.database", bindable);
                result = bindResult;
            }
            {
                bindResult.isBound();
                result = false;
            }
        };

        ZebraShardingConfiguration cfg = ZebraShardingConfigurationUtil.getZebraShardingConfiguration();

        assertThat(cfg, is(nullValue()));
    }

    @Test
    public void testGetCfg() {
        ZebraShardingConfiguration cfg = new ZebraShardingConfiguration();

        Map<String, ZebraShardingDataSourceCfg> cfgMap = new HashMap<>();
        ZebraShardingDataSourceCfg shardingCfg = new ZebraShardingDataSourceCfg();
        shardingCfg.setSharding(new ZebraShardingRuleConfiguration());
        ZebraShardingDataSourceCfg masterSlaveCfg = new ZebraShardingDataSourceCfg();
        masterSlaveCfg.setMasterslave(new ZebraMasterSlaveRuleConfiguration());
        cfgMap.put("shardingDs1", shardingCfg);
        cfgMap.put("masterSlaveDs1", masterSlaveCfg);

        cfg.setShardingcfg(cfgMap);

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
                @SuppressWarnings("unchecked")
                Bindable<ZebraShardingConfiguration> bindable = (Bindable<ZebraShardingConfiguration>) any;
                binder.bind("zebra.database", bindable);
                result = bindResult;
            }
            {
                bindResult.isBound();
                result = true;
            }
            {
                bindResult.get();
                result = cfg;
            }
        };

        ZebraShardingConfiguration result = ZebraShardingConfigurationUtil.getZebraShardingConfiguration();
        assertThat(result, is(not(nullValue())));
        Map<String, ZebraShardingDataSourceCfg> resultCfg = result.getShardingcfg();
        assertThat(resultCfg,  is(not(anEmptyMap())));

        ZebraShardingDataSourceCfg resultShardingCfg = resultCfg.get("shardingDs1");
        assertThat(resultShardingCfg.getType(),  is(ZebraShardingDataSourceCfg.Type.SHARDING));

        ZebraShardingDataSourceCfg resultMasterSlaveCfg = resultCfg.get("masterSlaveDs1");
        assertThat(resultMasterSlaveCfg.getType(),  is(ZebraShardingDataSourceCfg.Type.MASTER_SLAVE));
        assertThat(resultMasterSlaveCfg.getMasterslave().getName(),  is("masterSlaveDs1"));
    }
}
