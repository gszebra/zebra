package com.guosen.zebra.database.sharding.conf;

import com.alibaba.fastjson.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * zabra的分库分表配置，参考org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration的写法<br>
 * Sharding-JDBC的每一个sharding的配置都是一个独立的配置文件，而zebra的所有配置都在同一个配置文件中，
 * 为了支持多个sharding数据源的情况所以必须加上shardingDataSourceName前缀。<br>
 * 配置样例如下
 * <pre>
 * {@code
 *  zebra.database.shardingcfg.shardingDataSourceName1.xxx.yyyy
 *  ...
 *  zebra.database.shardingcfg.shardingDataSourceNameK.xxx.yyyy
 *  ...
 *  zebra.database.shardingcfg.shardingDataSourceNameN.xxx.yyyy
 * }
 * </pre>
 * 其中xxx.yyy参考ShardingSphere的springboot配置参考，为spring.shardingsphere后面部分<br/>
 * 注意使用Binder方式读取此配置，不能用@ConfigurationProperties方式自动读取
 * @see ZebraShardingConfigurationUtil
 */
public class ZebraShardingConfiguration {

    /**
     * Sharding数据源映射表<br/>
     * 对应相关配置项prefix为zebra.database.shardingcfg
     */
    private Map<String, ZebraShardingDataSourceCfg> shardingcfg = new LinkedHashMap<>();

    public Map<String, ZebraShardingDataSourceCfg> getShardingcfg() {
        return shardingcfg;
    }

    public void setShardingcfg(Map<String, ZebraShardingDataSourceCfg> shardingcfg) {
        this.shardingcfg = shardingcfg;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
