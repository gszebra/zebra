package com.guosen.zebra.core.common;

import com.guosen.zebra.core.grpc.util.PropertiesContent;
import io.grpc.Metadata;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.regex.Pattern;

@Component
public class ZebraConstants {
	public static final String KEY = "key";
	public static final String TTL = "ttl";
	public static final String VALUE = "value";
	public static final String INTERFACE_KEY = "interface";
	public static final String INTERFACECLASS_KEY = "interfaceClass";
	public static final String GRPC_STUB_KEY = "grpcstub";
	public static final String GRPC_FALLBACK_KEY = "grpcfallback";
	public static final String GENERIC_KEY = "generic";
	public static final String GROUP_KEY = "group";
	public static final String VERSION_KEY = "version";
	public static final String REGISTRY_RPC_PORT_KEY = "registryrpcport";
	public static final String HTTP_PORT_KEY = "httpport";
	public static final String MONITOR_INTERVAL = "monitorinterval";
	public static final String APPLICATION_NAME = "application";
	public static final String TIMEOUT = "timeout";
	public static final String DEFAULT_GROUP = "default";
	public static final String DEFAULT_SET = "0";
	public static final String DEFAULT_VERSION = "1.0.0";
	public static final String LOCALHOST_KEY = "localhost";
	public static final String ANYHOST_KEY = "anyhost";
	public static final String RETRY_METHODS_KEY = "retrymethods";
	public static final String FALLBACK_METHODS_KEY = "fallbackmethods";
	public static final String METHOD_KEY = "method";
	public static final String ARG_KEY = "arg";
	public static final String METHOD_RETRY_KEY = "retries";
	public static final String ANYHOST_VALUE = "0.0.0.0";
	public static final String MESSAGE_SIZE_KEY = "zebra.grpc.message.size";

	public static final String REGISTRY_RETRY_PERIOD_KEY = "retry.period";
	public static final int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;
	public static final String ENABLED_KEY = "enabled";
	public static final String DISABLED_KEY = "disabled";

	public static final String ETCD_SERVICE_PRE = "/zebra";
	public static final String PATH_SEPARATOR = "/";

	public static final String ASYNC_KEY = "async";
	public static final String ROUTE_KEY = "route";
	public static final int RPCTYPE_ASYNC = 1;
	public static final int RPCTYPE_BLOCKING = 2;
	public static final int RPC_ASYNC_DEFAULT_TIMEOUT = 5000;

	public static final String REMOTE_ADDRESS = "remote";

	public static final String VALIDATOR_GROUPS = "validator.groups";

	public static final String TYPE_SERVICE = "server";
	public static final String TYPE_CLIENT = "client";
	public static final String TYPE_PUB = "pub";
	public static final String TYPE_SUB = "sub";
	public static final String TYPE_MONITOR = "monitor";
	public static final String TYPE_GATEWATY = "gateway";
	public static final String TYPE_CONSOLE = "console";
	public static final String TYPE_CONF = "conf";

	public static final String SERVICE_NAME = "serviceName";

//	public static final String CUST_NAME = "custName";

	public static final String KEY_SET = "set";
	
	public static final String KEY_ADDR = "addr";

	public static final int CORE_POOL_SIZE = 30;
	public static final int MAX_POOL_SIZE = 200;
	public static final int QUEUE_CAPACITY = 1000;

	public static final String EXECUTOR_NAME = "zebra-biz-executor";
	
	public static final String EXECUTOR_NAME_REG = "zebra-biz-reg-executor";

	public static final String ZEBRA_SCAN_PACKGE = "com.guosen.*";

	public static final String ZEBRA_JSON_PREFIX = "JSON";

	public static final String ZEBRA_IS_PREFIX = "INPUTSREAM";

	public static final String ZEBRA_ETCD_CUST_TTL = "60";

	public static final String ZEBRA_SCOPE = "scope";
	public static final String ZEBRA_SCOPE_NAME = "scopeName";
	public static final String ZEBRA_SCOPE_GLOBAL = "global";
	public static final String ZEBRA_SCOPE_IDC = "idc";
	public static final String ZEBRA_SCOPE_SET = "set";
	public static final String ZEBRA_SCOPE_NODE = "node";


	public static final String KEY_CLIENT_INTERCEPTOR = "clientInterceptor";

	public static final String KEY_SERVER_INTERCEPTOR = "serverInterceptor";

	public static final String ZEBRA_CACHE_TASK_NAME = "zebraCacheThreadTask";

	public static final String ZEBRA_ENABLE_RET_LOG = "zebra.enable.retlog";

	public static final String ZEBRA_OPEN_TRACING = "opentracing";

	public final static String USER_TAG = "userCode";
	public final static String SESSION_TAG = "session";
	public final static String PKG_TAG = "pkg";
	public final static String H_PKG_TAG = "H_pkg";
	public final static String N_PKG_TAG = "N_pkg";
	public static final String HW_ID_TAG = "hwID";
	public static final String IP_TAG = "ip";
	public final static String H_PKG_PREFIX = "H_";
	public final static String N_PKG_PREFIX = "N_";
	
	public final static String KEY_CURRENT_ADDR = "currentAddr";
	
	public final static String SESSION_CHECH_KEY = "SESSION_CHECH_KEY";
	
	public final static Metadata.Key<String> remoteKey = Metadata.Key.of("remoteKey", Metadata.ASCII_STRING_MARSHALLER);
	
	
	/** 
	* @Fields ZEBRA_PROPERTIES_NAME : 配置名称可变
	*/ 
	public final static String ZEBRA_PROPERTIES_NAME = "localCache.properties";
	
	public final static String ZEBRA_TRACING_RETIO ="tracing.retio";
	
	
	public static final String OPEN_TARGET_IP_KEY="zebra.open.targetip";
	
	public static boolean IS_RECORD_TARGET_IP;
	
	public static boolean START_UP_FUSING = false;
	
	public final static String ZEBRA_LAZY_ROUTE ="zebra.lazy.route";
	public static final String ZEBRA_LAZY_LOAD_TIME_KEY="zebra.lazyLoad.time";
	
	public static final String ZEBRA_GATEWAY_NAME="zebra.gateway";
	
	public static final String ZEBRA_MONITOR_NAME="zebra.monitor";
	
	public static final String ZEBRA_CONSOLE_NAME="zebra.console";
	
	public static final int SUCCESS=0;
	
	public static final int FAIL=1;
	
	public static final String KEY_CODE="code";
	
	public static final String KEY_DATA="data";

	@PostConstruct
	public void init(){
			IS_RECORD_TARGET_IP = PropertiesContent.getbooleanValue(OPEN_TARGET_IP_KEY);
	}
}
