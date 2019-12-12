/**   
* @Title: SentinelDTO.java 
* @Package com.guosen.zebra.conf.dto 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年12月3日 下午1:23:36 
* @version V1.0   
*/
package com.guosen.zebra.conf.dto;

/** 
* @ClassName: SentinelDTO 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2018年12月3日 下午1:23:36 
*  
*/
public class SentinelDTO {
	private int id;
	private String type;
	private String serverName;
	private String ip;
	private String data;
	@Override
	public String toString() {
		return "SentinelDTO [id=" + id + ", type=" + type + ", serverName=" + serverName + ", ip=" + ip + ", data="
				+ data + "]";
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
}
