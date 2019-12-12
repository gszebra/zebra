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
public class ConfCenter {
	private int sid;
	private int type;
	private String server;
	private String scope;
	private String scopeName;
	private String text;
	private String date;
	private String versionInfo;
	
	public int getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getScopeName() {
		return scopeName;
	}
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
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
	@Override
	public String toString() {
		return "ConfCenter [sid=" + sid + ", type=" + type + ", server=" + server + ", scope=" + scope + ", scopeName="
				+ scopeName + ", text=" + text + ", date=" + date + ", versionInfo=" + versionInfo + "]";
	}
}
