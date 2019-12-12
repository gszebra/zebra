package com.guosen.zebra.console.strategy;

import java.util.Map;

import com.coreos.jetcd.data.KeyValue;
import com.guosen.zebra.console.dto.ServiceInfo;

public class ServerStrategy implements AssembleStrategy {
	public void excute(ServiceInfo info, KeyValue kv, Map<String, ServiceInfo> serviceMap) {
		info.getAddrs().add(kv.getValue().toStringUtf8());
	}
}
