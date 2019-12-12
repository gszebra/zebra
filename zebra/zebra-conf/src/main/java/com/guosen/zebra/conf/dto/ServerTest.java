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
public class ServerTest {
	private String server;
	private String method;
	private String request;
	private String response;
	private String attachments;
	private String descript;
	@Override
	public String toString() {
		return "ServerTest [server=" + server + ", method=" + method + ", request=" + request + ", response=" + response
				+ ", attachments=" + attachments + ", desc=" + getDescript() + "]";
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getAttachments() {
		return attachments;
	}
	public void setAttachments(String attachments) {
		this.attachments = attachments;
	}
	public String getDescript() {
		return descript;
	}
	public void setDescript(String descript) {
		this.descript = descript;
	}
}
