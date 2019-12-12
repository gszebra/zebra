package com.guosen.zebra.core.registry.etcd.cmd;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.options.GetOption;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.registry.etcd.EtcdClient;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;
import com.guosen.zebra.core.route.Route;

public class CommandGet {
	private static final Logger log = LogManager.getLogger(CommandGet.class);

	// get executes the "get" command.
	public void get(Map<String, Object> keyValue, List<KeyValue> ret) throws Exception {
		try {
			String prefix = (String) keyValue.get(ZebraConstants.KEY);
			ByteSequence key = ByteSequence.fromString(prefix);
			GetResponse getResponse = EtcdClient.getSingleton().client.getKVClient()
					.get(key, GetOption.newBuilder().withPrefix(key).build()).get();
			if (getResponse.getKvs().isEmpty()) {
				return;
			}
			List<KeyValue> kvs = getResponse.getKvs().stream()
					.filter(kv -> kv.getKey().toStringUtf8().indexOf(prefix) >= 0).collect(Collectors.toList());
			if (!StringUtils.isEmpty((String) keyValue.get(ZebraConstants.ROUTE_KEY))
					&& prefix.indexOf(EtcdRegistry.getServiceName()) > 0) {
				final String route = (String) keyValue.get(ZebraConstants.ROUTE_KEY);
				kvs = Route.routeFilter(route, kvs);
			}
			ret.addAll(kvs);
		} catch (Exception e) {
			log.error("etcd client get key {} err" , keyValue);
			throw e;
		}
	}
}
