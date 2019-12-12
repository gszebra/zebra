package com.guosen.zebra.console.strategy;

import java.util.Map;

import com.coreos.jetcd.data.KeyValue;
import com.guosen.zebra.console.dto.ServiceInfo;

public class ServiceStrategyFactory {
	public static void run(ServiceInfo info, KeyValue kv, Map<String, ServiceInfo> serviceMap) {
		AssembleStrategy strategy = null;
		if ("gateway".equals(info.getType())) {
			strategy = new GatewayStrategy();
		} else if ("monitor".equals(info.getType())) {
			strategy = new MonitorStrategy();
		} else if ("console".equals(info.getType())) {
			strategy = new ConsoleStrategy();
		} else if ("conf".equals(info.getType())) {
			strategy = new ConfStrategy();
		} else if ("server".equals(info.getType())) {
			strategy = new ServerStrategy();
		} else if ("client".equals(info.getType())) {
			strategy = new ClientStrategy();
		} else if ("pub".equals(info.getType())) {
			strategy = new PubStrategy();
		} else if ("sub".equals(info.getType())) {
			strategy = new SubStrategy();
		}
		strategy.excute(info, kv, serviceMap);
	}
}
