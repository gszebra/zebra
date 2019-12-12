package com.guosen.zebra.database.mybatis.conf;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

public class DataSourceCfg {
    /**
     * 数据源名称
     */
    private String dataSourceName;

    /**
     * 数据库连接URL
     */
    private String url;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码<br/>
     * 密码不打印，以避免出现安全问题
     */
    @JSONField(serialize = false)
    private String password;

    /**
     * 驱动类名称<br/>
     * 在Druid数据源不能识别时，需要开发者自行配置
     */
    private String driverClass;

    /**
     * MyBatis Mapper对应的base package
     */
    private String basePackage;

    public static class Builder{
        private DataSourceCfg dataSourceCfg = new DataSourceCfg();

        private Builder(){}

        public static Builder newBuilder() {
            return new Builder();
        }

        public DataSourceCfg build() {
            return dataSourceCfg;
        }

        public Builder dataSourceName(String dataSourceName) {
            dataSourceCfg.dataSourceName = dataSourceName;
            return this;
        }

        public Builder url(String url) {
            dataSourceCfg.url = url;
            return this;
        }

        public Builder userName(String userName) {
            dataSourceCfg.userName = userName;
            return this;
        }

        public Builder password(String password) {
            dataSourceCfg.password = password;
            return this;
        }

        public Builder driverClass(String driverClass) {
            dataSourceCfg.driverClass = driverClass;
            return this;
        }

        public Builder basePackage(String basePackage) {
            dataSourceCfg.basePackage = basePackage;
            return this;
        }
    };

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
