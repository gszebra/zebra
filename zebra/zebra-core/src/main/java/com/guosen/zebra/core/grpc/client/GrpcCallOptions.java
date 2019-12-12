package com.guosen.zebra.core.grpc.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Maps;
import com.guosen.zebra.core.common.ZebraConstants;

import io.grpc.CallOptions;

public abstract class GrpcCallOptions {
	public static final CallOptions.Key<ConcurrentHashMap<String, Object>> CALLOPTIONS_CUSTOME_KEY = CallOptions.Key.createWithDefault("custom_options", new ConcurrentHashMap<String, Object>());

	public static final String GRPC_REF_URL = "grpc-refurl";

	public static final String GRPC_NAMERESOVER_ATTRIBUTES = "nameresolver-attributes";
	
	public static final String GRPC_CURRENT_ADDR_KEY = "current-address";

	private static final Map<String, CallOptions> CACHEOPTIONS_CACHE = Maps.newConcurrentMap();
	
	@SuppressWarnings("unchecked")
	public static CallOptions createCallOptions(Map<String, Object> refUrl) {
		String serviceName = (String) refUrl.get(ZebraConstants.SERVICE_NAME);
		CallOptions options = CACHEOPTIONS_CACHE.get(serviceName);
		if (options == null) {
			ConcurrentHashMap<String, Object> customOptions = new ConcurrentHashMap<String, Object>();
			HashMap<String, Object> newMap = (HashMap<String, Object>) ((HashMap<String, Object>)refUrl).clone();
			newMap.remove("arg");
			customOptions.put(GRPC_REF_URL, newMap);
			options = CallOptions.DEFAULT.withOption(CALLOPTIONS_CUSTOME_KEY, customOptions);
			CACHEOPTIONS_CACHE.put(serviceName, options);
			return options;
		} else {
			return options;
		}
	}
	
	public static Map<String, Object> getAffinity(Map<String, Object> refUrl) {
		String serviceName = (String) refUrl.get(ZebraConstants.SERVICE_NAME);
		return CACHEOPTIONS_CACHE.get(serviceName).getOption(CALLOPTIONS_CUSTOME_KEY);
	}
}
