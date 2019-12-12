/**   
* @Title: User.java 
* @Package com.yy.bg.domain 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年5月18日 下午3:54:00 
* @version V1.0   
*/
package com.guosen.zebra.conf.dto;

/** 
* @ClassName: User 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2017年5月18日 下午3:54:00 
*  
*/
public class GatwayConf {
	private int sid;
	private String service;
	private String group;
	private String version;
	private String set;
	private String path;
	private String text;
	private String tag;
	private String gatewaySet;
	private String date;
	private String versionInfo;
	@Override
	public String toString() {
		return "GatwayConf [sid=" + sid + ", service=" + service + ", group=" + group + ", version=" + version
				+ ", set=" + set + ", path=" + path + ", text=" + text + ", tag=" + tag + ", gatewaySet=" + gatewaySet
				+ ", date=" + date + ", versionInfo=" + versionInfo + "]";
	}
	public int getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getSet() {
		return set;
	}
	public void setSet(String set) {
		this.set = set;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getGatewaySet() {
		return gatewaySet;
	}
	public void setGatewaySet(String gatewaySet) {
		this.gatewaySet = gatewaySet;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getVersionInfo() {
		return versionInfo;
	}
	public void setVersionInfo(String versionInfo) {
		this.versionInfo = versionInfo;
	}
}
