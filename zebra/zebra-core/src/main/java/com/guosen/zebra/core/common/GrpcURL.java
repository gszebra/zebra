package com.guosen.zebra.core.common;

import java.util.List;

import com.coreos.jetcd.data.KeyValue;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;

/** 
* @ClassName: GrpcURL 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2017年10月30日 下午3:46:33 
*  
*/
public class GrpcURL {
	private RpcServiceBaseInfo serviceConf;
	private List<KeyValue> remoteServiceAddrs;
	public RpcServiceBaseInfo getServiceConf() {
		return serviceConf;
	}
	public void setServiceConf(RpcServiceBaseInfo serviceConf) {
		this.serviceConf = serviceConf;
	}
	public List<KeyValue> getRemoteServiceAddrs() {
		return remoteServiceAddrs;
	}
	public void setRemoteServiceAddrs(List<KeyValue> remoteServiceAddrs) {
		this.remoteServiceAddrs = remoteServiceAddrs;
	}
}
