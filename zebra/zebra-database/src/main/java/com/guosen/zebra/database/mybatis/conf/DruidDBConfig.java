/**   
* @Title: AbstractDruidDBConfig.java 
* @Package com.guosen.zebra.database.mybatis.conf 
* @author 邓启翔
* @date 2018年1月23日 下午3:14:03 
* @version V1.0   
*/
package com.guosen.zebra.database.mybatis.conf;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.guosen.zebra.database.mybatis.properties.DruidDbProperties;
import com.guosen.zebra.database.utils.OperateProperties;

import java.util.Properties;

/**
 * @ClassName: AbstractDruidDBConfig
 * @author 邓启翔
 * @date 2018年1月23日 下午3:14:03
 * 
 */
public final class DruidDBConfig {

	private static final Supplier<DruidDBConfig> INSTANCE = Suppliers.memoize(DruidDBConfig::getDruidDBConfig);

	private DruidDbProperties druidDbProperties;

	private DruidDBConfig(){}

	private static DruidDBConfig getDruidDBConfig() {
		DruidDbProperties properties = new DruidDbProperties();
		Properties p = OperateProperties.p;
		if (p.get("initialSize") != null) {
			properties.setInitialSize(OperateProperties.getIntValue("initialSize", 10));
		}
		if (p.get("minIdle") != null) {
			properties.setMinIdle(OperateProperties.getIntValue("minIdle", 50));
		}
		if (p.get("maxActive") != null) {
			properties.setMaxActive(OperateProperties.getIntValue("maxActive", 300));
		}
		if (p.get("maxWait") != null) {
			properties.setMaxWait(OperateProperties.getIntValue("maxWait", 60000));
		}
		if (p.get("timeBetweenEvictionRunsMillis") != null) {
			properties.setTimeBetweenEvictionRunsMillis(
					OperateProperties.getIntValue("timeBetweenEvictionRunsMillis", 60000));
		}
		if (p.get("minEvictableIdleTimeMillis") != null) {
			properties.setMinEvictableIdleTimeMillis(
					OperateProperties.getIntValue("minEvictableIdleTimeMillis", 3600000));
		}
		if (p.get("validationQuery") != null) {
			properties.setValidationQuery(OperateProperties.getStrValue("validationQuery", "SELECT 1"));
		}
		if (p.get("testWhileIdle") != null) {
			properties.setTestWhileIdle(OperateProperties.getBooleanValue("testWhileIdle"));
		}
		if (p.get("testOnBorrow") != null) {
			properties.setTestOnBorrow(OperateProperties.getBooleanValue("testOnBorrow"));
		}
		if (p.get("testOnReturn") != null) {
			properties.setTestOnReturn(OperateProperties.getBooleanValue("testOnReturn"));
		}
		DruidDBConfig conf = new DruidDBConfig();
		conf.setDruidDbProperties(properties);
		return conf;
	}

	public static DruidDBConfig getInstance() {
		return INSTANCE.get();
	}

	public DruidDbProperties getDruidDbProperties() {
		return druidDbProperties;
	}

	private void setDruidDbProperties(DruidDbProperties druidDbProperties) {
		this.druidDbProperties = druidDbProperties;
	}
}