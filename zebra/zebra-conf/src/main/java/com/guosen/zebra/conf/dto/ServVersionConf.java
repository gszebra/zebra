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
public class ServVersionConf {
	private String SERVER_NAME;
	private int SID;
	private String SERVER_VERSION;
	private String SERVER_VERSION_DESC;
	public String getSERVER_NAME() {
		return SERVER_NAME;
	}
	public void setSERVER_NAME(String sERVER_NAME) {
		SERVER_NAME = sERVER_NAME;
	}
	public int getSID() {
		return SID;
	}
	public void setSID(int sID) {
		SID = sID;
	}
	public String getSERVER_VERSION() {
		return SERVER_VERSION;
	}
	public void setSERVER_VERSION(String sERVER_VERSION) {
		SERVER_VERSION = sERVER_VERSION;
	}
	public String getSERVER_VERSION_DESC() {
		return SERVER_VERSION_DESC;
	}
	public void setSERVER_VERSION_DESC(String sERVER_VERSION_DESC) {
		SERVER_VERSION_DESC = sERVER_VERSION_DESC;
	}

}
