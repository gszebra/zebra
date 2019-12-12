package com.guosen.zebra.database.mybatis.conf;

import com.guosen.zebra.database.utils.OperateProperties;
import mockit.Expectations;
import mockit.Mocked;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class DataSourceCfgUtilTest {

    @Mocked
    private OperateProperties operateProperties;

    @Test
    public void testGetDataSourceCfgList() {
        final String ds0Url = "www.baidu.com";
        final String ds0BasePackage = "com.guosen.zebra.ping";
        final String ds0DataSourceName = "ds0";
        final String ds0Username = "user0";
        final String ds0Password = "password0";
        final String ds0DriverClass = "com.hello.world";

        final String ds1Url = "www.guosen.com";
        final String ds1BasePackage = "com.guosen.zebra.pong";
        final String ds1DataSourceName = "ds1";
        final String ds1Username = "user1";
        final String ds1Password = "password1";
        final String ds1DriverClass = "com.foo.bar";

        new Expectations() {
            {
                OperateProperties.getStrValue("zebra.database.url[0]", StringUtils.EMPTY);
                result = ds0Url;
            }
            {
                OperateProperties.getStrValue("zebra.database.basePackage[0]", StringUtils.EMPTY);
                result = ds0BasePackage;
            }
            {
                OperateProperties.getStrValue("zebra.database.dataSourceName[0]", StringUtils.EMPTY);
                result = ds0DataSourceName;
            }
            {
                OperateProperties.getStrValue("zebra.database.username[0]", StringUtils.EMPTY);
                result = ds0Username;
            }
            {
                OperateProperties.getStrValue("zebra.database.pwd[0]", StringUtils.EMPTY);
                result = ds0Password;
            }
            {
                OperateProperties.getStrValue("zebra.database.driverClass[0]", StringUtils.EMPTY);
                result = ds0DriverClass;
            }
            {
                OperateProperties.getStrValue("zebra.database.url[1]", StringUtils.EMPTY);
                result = ds1Url;
            }
            {
                OperateProperties.getStrValue("zebra.database.basePackage[1]", StringUtils.EMPTY);
                result = ds1BasePackage;
            }
            {
                OperateProperties.getStrValue("zebra.database.dataSourceName[1]", StringUtils.EMPTY);
                result = ds1DataSourceName;
            }
            {
                OperateProperties.getStrValue("zebra.database.username[1]", StringUtils.EMPTY);
                result = ds1Username;
            }
            {
                OperateProperties.getStrValue("zebra.database.pwd[1]", StringUtils.EMPTY);
                result = ds1Password;
            }
            {
                OperateProperties.getStrValue("zebra.database.driverClass[1]", StringUtils.EMPTY);
                result = ds1DriverClass;
            }
        };

        List<DataSourceCfg> dataSourceCfgs = DataSourceCfgUtil.getDataSourceCfgList();

        assertThat(dataSourceCfgs,  is(not(empty())));
        assertThat(dataSourceCfgs.size(), is(2));

        DataSourceCfg cfg0 = dataSourceCfgs.get(0);

        assertThat(cfg0.getUrl(), is(ds0Url));
        assertThat(cfg0.getBasePackage(), is(ds0BasePackage));
        assertThat(cfg0.getDataSourceName(), is(ds0DataSourceName));
        assertThat(cfg0.getUserName(), is(ds0Username));
        assertThat(cfg0.getPassword(), is(ds0Password));
        assertThat(cfg0.getDriverClass(), is(ds0DriverClass));


        DataSourceCfg cfg1 = dataSourceCfgs.get(1);

        assertThat(cfg1.getUrl(), is(ds1Url));
        assertThat(cfg1.getBasePackage(), is(ds1BasePackage));
        assertThat(cfg1.getDataSourceName(), is(ds1DataSourceName));
        assertThat(cfg1.getUserName(), is(ds1Username));
        assertThat(cfg1.getPassword(), is(ds1Password));
        assertThat(cfg1.getDriverClass(), is(ds1DriverClass));
    }

    @Test
    public void testUseNameBlank() {
        final String ds0Url = "www.baidu.com";
        final String ds0BasePackage = "com.guosen.zebra.ping";
        final String ds0DataSourceName = "ds0";
        final String ds0Username = StringUtils.EMPTY;

        new Expectations() {
            {
                OperateProperties.getStrValue("zebra.database.url[0]", StringUtils.EMPTY);
                result = ds0Url;
            }
            {
                OperateProperties.getStrValue("zebra.database.basePackage[0]", StringUtils.EMPTY);
                result = ds0BasePackage;
            }
            {
                OperateProperties.getStrValue("zebra.database.dataSourceName[0]", StringUtils.EMPTY);
                result = ds0DataSourceName;
            }
            {
                OperateProperties.getStrValue("zebra.database.username[0]", StringUtils.EMPTY);
                result = ds0Username;
            }
        };

        try {
            DataSourceCfgUtil.getDataSourceCfgList();
            fail("Must have exception.");
        }
        catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), equalTo("zebra.database.username[0] must be config."));
        }

    }

    @Test
    public void testDataSourceNameBlank() {

        final String ds0Url = "www.baidu.com";
        final String ds0BasePackage = "com.guosen.zebra.ping";

        new Expectations() {
            {
                OperateProperties.getStrValue("zebra.database.url[0]", StringUtils.EMPTY);
                result = ds0Url;
            }
            {
                OperateProperties.getStrValue("zebra.database.basePackage[0]", StringUtils.EMPTY);
                result = ds0BasePackage;
            }
            {
                OperateProperties.getStrValue("zebra.database.dataSourceName[0]", StringUtils.EMPTY);
                result = StringUtils.EMPTY;
            }
        };

        try {
            DataSourceCfgUtil.getDataSourceCfgList();
            fail("Must have exception.");
        }
        catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), equalTo("zebra.database.dataSourceName[0] must be config."));
        }
    }
}
