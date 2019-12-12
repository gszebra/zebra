package com.guosen.zebra.database.mybatis.conf;

import org.junit.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DataSourceCfgTest {

    @Test
    public void testWithoutPassword() {
        DataSourceCfg dataSourceCfg = new DataSourceCfg();
        dataSourceCfg.setDataSourceName("ds01");
        dataSourceCfg.setUserName("sa");
        dataSourceCfg.setPassword("helloWorld");
        dataSourceCfg.setBasePackage("com.guosen.zebra.test");
        dataSourceCfg.setUrl("www.baidu.com");

        String dataSourceCfgStr = dataSourceCfg.toString();

        assertThat(dataSourceCfgStr, not(containsString("password")));
    }
}
