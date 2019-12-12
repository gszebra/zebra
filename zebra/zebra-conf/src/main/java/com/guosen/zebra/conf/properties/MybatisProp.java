/**   
* @Title: MybatisProp.java 
* @Package com.yy.bg.properties 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年5月18日 下午5:16:56 
* @version V1.0   
*/
package com.guosen.zebra.conf.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 
* @ClassName: MybatisProp 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2017年5月18日 下午5:16:56 
*  
*/
@ConfigurationProperties("mybatis")
public class MybatisProp {
	private String typeAliasesPackage; 
	private String mapperLocations;
	public String getTypeAliasesPackage() {
		return typeAliasesPackage;
	}
	public void setTypeAliasesPackage(String typeAliasesPackage) {
		this.typeAliasesPackage = typeAliasesPackage;
	}
	public String getMapperLocations() {
		return mapperLocations;
	}
	public void setMapperLocations(String mapperLocations) {
		this.mapperLocations = mapperLocations;
	} 
}
