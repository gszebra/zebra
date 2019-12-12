package com.guosen.zebra.database.init;

import io.opentracing.Tracer;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.apache.shardingsphere.opentracing.ShardingTracer;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class ShardingDbInitTest {

    @Tested
    private ShardingDbInit shardingDbInit;

    @Mocked
    private BeanDefinitionRegistry registry;

    @Mocked
    private ShardingTracer shardingTracer;


    /**
     * 测试没有sharding配置的情况
     */
    @Test
    public void testNoShardingCfg() {
        shardingDbInit.init(registry, null, null);

        new Verifications() {
            {
                ShardingTracer.init((Tracer)any);
                times = 0;
            }
        };
    }

    
}
