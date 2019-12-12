package com.guosen.zebra.core.grpc.server;

import java.io.InputStream;

import com.alibaba.fastjson.JSONObject;

public interface GenericService {
	/** 
	* @Title: $invoke 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param @param serviceName
	* @param @param group
	* @param @param version
	* @param @param method
	* @param @param arg
	* @param @return    设定文件 
	* @return Object    
	* @throws 
	*/
	JSONObject $invoke(String serviceName, String group, String version, String method, JSONObject arg);
	
	/** 
	* @Title: $invoke 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param @param serviceName
	* @param @param group
	* @param @param set
	* @param @param version
	* @param @param method
	* @param @param arg
	* @param @return    设定文件 
	* @return JSONObject    返回类型 
	* @throws 
	*/
	JSONObject $invoke(String serviceName, String group, String version,String set, String method, JSONObject arg);
	/** 
	* @Title: $invoke 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param @param serviceName
	* @param @param group
	* @param @param version
	* @param @param method
	* @param @param arg
	* @param @return    设定文件 
	* @return Object    
	* @throws 
	*/
	InputStream $invoke(String serviceName, String group, String version,String set, String method, InputStream arg);
	
	/** 
	* @Title: $invoke 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param @param serviceName
	* @param @param group
	* @param @param set
	* @param @param addr
	* @param @param version
	* @param @param method
	* @param @param arg
	* @param @return    设定文件 
	* @return JSONObject    返回类型 
	* @throws 
	*/
	JSONObject $invoke(String serviceName, String group, String version,String set,String addr, String method, JSONObject arg);
}
