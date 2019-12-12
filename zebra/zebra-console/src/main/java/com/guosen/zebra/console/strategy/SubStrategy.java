package com.guosen.zebra.console.strategy;

import java.util.Map;

import com.coreos.jetcd.data.KeyValue;
import com.guosen.zebra.console.dto.Line;
import com.guosen.zebra.console.dto.ServiceInfo;

public class SubStrategy implements AssembleStrategy {
	public void excute(ServiceInfo info, KeyValue kv, Map<String, ServiceInfo> serviceMap) {
		String val = kv.getValue().toStringUtf8();
		String fromName = val.split(":")[0];
		if (("console".equals(fromName)) || ("conf".equals(fromName)) || ("monitor".equals(fromName))
				|| ("gateway".equals(fromName)) || ("zebra-console".equals(fromName)) || ("zebra-conf".equals(fromName))
				|| ("zebra-monitor".equals(fromName)) || ("zebra-gateway".equals(fromName))) {
			return;
		}
		String[] args = kv.getKey().toStringUtf8().split("/");
		String to = args[3];
		Line line = new Line();
		line.setFrom(fromName);
		line.setTo(to);

		info.getAddrs().add(val.substring(val.indexOf(":") + 1));
		if (!info.getLines().contains(line)) {
			info.getLines().add(line);
		}
	}
}
