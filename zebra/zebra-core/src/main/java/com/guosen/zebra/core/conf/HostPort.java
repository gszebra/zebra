/**   
* @Title: HostPort.java 
* @Package com.guosen.zebra.core.conf 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年10月31日 下午4:00:51 
* @version V1.0   
*/
package com.guosen.zebra.core.conf;

import java.util.Map;

/** 
* @ClassName: HostPort 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2017年10月31日 下午4:00:51 
*  
*/
public class HostPort {
	private String host;
	private int port;
	private Map<String, String> parameters;
	private String path;
	public HostPort(String host,int port,String path,Map<String, String> parameters){
		this.host = host;
		this.port= port;
		this.parameters=parameters;
		this.path = path;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	@Override
	public String toString() {
		return "HostPort [host=" + host + ", port=" + port + ", parameters=" + parameters + ", path=" + path + "]";
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}
