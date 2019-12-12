package com.guosen.zebra.database.sharding.conf;

import com.alibaba.fastjson.JSONObject;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Properties;

/**
 * sharding数据源的具体配置<br/>
 * 此处支持的配置对应ShardingJDBC的配置，请参考
 * <a href="https://shardingsphere.apache.org/document/current/cn/manual/sharding-jdbc/configuration/config-spring-boot/">Spring Boot配置</a>
 */
public class ZebraShardingDataSourceCfg {

    /**
     * 类型
     */
    public enum Type {
        /**
         * 分库分表
         */
        SHARDING("sharding"),

        /**
         * 读写分离
         */
        MASTER_SLAVE("masterSlave");

        private String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    /**
     * 配置类型，用于标志对应ShardingSphere的配置类型，开发者不能配置此字段。
     */
    private Type type;


    /**
     * 组成sharding数据源的普通数据源信息列表，此处datasource不采用驼峰是为了和ShardingSphere的配置保持一致
     */
    private ZebraReferDataSource datasource;

    /**
     * MyBatis Mapper的base package<br/>
     * 此配置为zebra定制的配置
     */
    @NotNull
    @Valid
    private String basePackage;

    /**
     * 分库分表规则配置
     */
    private ZebraShardingRuleConfiguration sharding;

    /**
     * 其他配置，比如sql.show等
     */
    private Properties props = new Properties();

    /**
     * 读写分离规则配置
     */
    private ZebraMasterSlaveRuleConfiguration masterslave;

    /*
     * 不支持项
     * spring.shardingsphere.datasource // datasource由zebra data source进行定义，此处千万不要添加进来
     *
     * 下列项后续有需要再支持
     * spring.shardingsphere.encrypt
     */

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ZebraReferDataSource getDatasource() {
        return datasource;
    }

    public void setDatasource(ZebraReferDataSource datasource) {
        this.datasource = datasource;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public ZebraShardingRuleConfiguration getSharding() {
        return sharding;
    }

    public void setSharding(ZebraShardingRuleConfiguration sharding) {
        this.sharding = sharding;
    }

    public ZebraMasterSlaveRuleConfiguration getMasterslave() {
        return masterslave;
    }

    public void setMasterslave(ZebraMasterSlaveRuleConfiguration masterslave) {
        this.masterslave = masterslave;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
