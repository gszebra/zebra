package com.guosen.zebra.database.mybatis.conf;

import com.google.common.collect.Sets;
import com.guosen.zebra.database.utils.OperateProperties;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Zebra数据源配置工具类，对应zebra.database的相关配置解析
 */
public final class DataSourceCfgUtil {

    /**
     * 支持配置的最大数据源个数
     */
    private static final int MAX_DATA_SOURCE_COUNT = 10;

    /**
     * URL key格式
     */
    private static final String URL_KEY_FORMAT = "zebra.database.url[%d]";

    /**
     * basePackage key格式
     */
    private static final String BASE_PACKAGE_FORMAT = "zebra.database.basePackage[%d]";

    /**
     * dataSourceName key格式
     */
    private static final String DATA_SOURCE_NAME_KEY_FORMAT = "zebra.database.dataSourceName[%d]";

    /**
     * username key格式
     */
    private static final String USER_NAME_KEY_FORMAT = "zebra.database.username[%d]";

    /**
     * pwd key格式
     */
    private static final String PASSWORD_KEY_FORMAT = "zebra.database.pwd[%d]";

    /**
     * driverClass key格式
     */
    private static final String DRIVER_CLASS_KEY_FORMAT = "zebra.database.driverClass[%d]";

    /**
     * 可以为空的配置format
     */
    private static final Set<String> BLANK_ABLE_CONF_FORMAT = Sets.newHashSet(
            URL_KEY_FORMAT,           // URL作为判断解析是否终止的字段
            BASE_PACKAGE_FORMAT,      // 被sharding引用的数据源base package可以为空
            PASSWORD_KEY_FORMAT,      // 有时候密码为空（比如开发环境）
            DRIVER_CLASS_KEY_FORMAT   // driver只在druid不能自动识别时才要配置
    );

    private DataSourceCfgUtil(){}

    /**
     * 获取zebra数据源配置信息
     * @return zebra数据源配置列表
     */
    public static List<DataSourceCfg> getDataSourceCfgList() {
        List<DataSourceCfg> dataSourceCfgList = new ArrayList<>(MAX_DATA_SOURCE_COUNT);
        for (int i = 0; i < MAX_DATA_SOURCE_COUNT; i++) {
            DataSourceCfg dataSourceCfg = getDataSourceCfg(i);
            if (dataSourceCfg == null) {
                break;
            }

            dataSourceCfgList.add(dataSourceCfg);
        }

        return dataSourceCfgList;
    }

    private static DataSourceCfg getDataSourceCfg(int index) {
        DataSourceCfg dataSourceCfg = null;

        String url = getValue(URL_KEY_FORMAT, index);
        if (StringUtils.isNotBlank(url)) {
            String basePackage = getValue(BASE_PACKAGE_FORMAT, index);
            String dataSourceName = getValue(DATA_SOURCE_NAME_KEY_FORMAT, index);
            String userName = getValue(USER_NAME_KEY_FORMAT, index);
            String password = getValue(PASSWORD_KEY_FORMAT, index);
            String driverClass = getValue(DRIVER_CLASS_KEY_FORMAT, index);

            dataSourceCfg = DataSourceCfg.Builder.newBuilder()
                    .dataSourceName(dataSourceName)
                    .url(url)
                    .basePackage(basePackage)
                    .userName(userName)
                    .password(password)
                    .driverClass(driverClass)
                    .build();
        }

        return dataSourceCfg;
    }

    private static String getValue(String keyFormat, int index) {
        String key = String.format(keyFormat, index);
        String value = OperateProperties.getStrValue(key, StringUtils.EMPTY);

        // 在解析时先把可以检查的先做了
        if (StringUtils.isBlank(value) && !BLANK_ABLE_CONF_FORMAT.contains(keyFormat)) {
            throw new IllegalArgumentException(key + " must be config.");
        }

        return value;
    }
}
