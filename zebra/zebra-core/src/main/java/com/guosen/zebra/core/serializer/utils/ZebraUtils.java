/**   
* @Title: ZebraUtils.java 
* @Package com.guosen.zebra.core.serializer.utils 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年12月13日 下午3:32:01 
* @version V1.0   
*/
package com.guosen.zebra.core.serializer.utils;

import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;

/** 
* @ClassName: ZebraUtils 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2017年12月13日 下午3:32:01 
*  
*/
public class ZebraUtils {
	public static RpcServiceBaseInfo getServiceInfo(String key) {
		RpcServiceBaseInfo serviceInfo = new RpcServiceBaseInfo();
		try{
			String args[] =key.split(ZebraConstants.PATH_SEPARATOR);
			String type = null;
			String fullServiceName =null;
			String group =null;
			String version =null;
			String set =null;
			String nodeId = null;
			if(args.length==8){
				type = args[6];
			}else if(args.length==7){
				type = args[3];
			}else{
				type = args[2];
			}
			if(ZebraConstants.TYPE_CLIENT.equals(type)) return serviceInfo;
			if(ZebraConstants.TYPE_GATEWATY.equals(type)){
				fullServiceName = type;
				group = args[2];
				version = args[4];
				set = args[5];
				nodeId =args[6];
			}else if(ZebraConstants.TYPE_MONITOR.equals(type)){
				fullServiceName = type;
			}else{
				group = args[2];
				fullServiceName = args[3];
				version = args[4];
				set = args[5];
				nodeId =args[7];
			}
			serviceInfo.setType(type);
			serviceInfo.setGroup(group);
			serviceInfo.setVersion(version);
			serviceInfo.setSet(set);
			serviceInfo.setService(fullServiceName);
			serviceInfo.setNodeId(nodeId);
		}catch(Exception e){
			throw e;
		}
		return serviceInfo;
	}
}
