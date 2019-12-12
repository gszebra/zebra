package com.guosen.zebra.database.sharding.conf;

import com.alibaba.fastjson.JSONObject;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingRuleConfiguration;


/**
 * Sharding rule configuration properties.<br/>
 *
 * @see org.apache.shardingsphere.shardingjdbc.spring.boot.sharding.SpringBootShardingRuleConfigurationProperties
 */
public class ZebraShardingRuleConfiguration extends YamlShardingRuleConfiguration {

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
