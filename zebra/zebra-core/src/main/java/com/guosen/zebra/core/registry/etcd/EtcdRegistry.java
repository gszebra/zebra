package com.guosen.zebra.core.registry.etcd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.coreos.jetcd.data.KeyValue;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.guosen.zebra.ZebraRun;
import com.guosen.zebra.core.boot.autoconfigure.GrpcProperties;
import com.guosen.zebra.core.common.GrpcURL;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;
import com.guosen.zebra.core.grpc.util.NetUtils;
import com.guosen.zebra.core.grpc.util.PropertiesContent;

/**
 * @ClassName: EtcdRegistry
 * @Description: Etcd注册类
 * @author 邓启翔
 * @date 2017年10月30日 下午3:50:07
 * 
 */
@Service
@ConditionalOnProperty(prefix = "zebra.grpc", value = "registryAddress")
public class EtcdRegistry {
	private static final Logger log = LogManager.getLogger(EtcdRegistry.class);
	
	public static Cache<RpcServiceBaseInfo, GrpcURL> serviceCache = CacheBuilder.newBuilder().maximumSize(10000)
			.build();

	public static String serverUuid;

	public static String clientUuid;
	
	private static String serviceName;

	@Autowired
	private GrpcProperties grpcProperties;
	
	public static int port;
	
	private ThreadPoolTaskExecutor regExecutor;
	
	@PostConstruct
	public void init(){
		System.err.println("begin to init etcd registry!!!!!!!");
	}
	
	public static String getServiceName(){
		if(serviceName ==null){
			File directory = new File("");
			String custName=null;
	        try {
				String scourseFile = directory.getCanonicalPath();
				String arg [] =scourseFile.split("\\\\");
				if(arg.length ==1){
					arg = scourseFile.split("/");
				}
				if(arg.length>0){
					custName = arg[arg.length-1];
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	        directory=null;
	        serviceName = custName;
		}
		return serviceName;
	}
	public synchronized void register(RpcServiceBaseInfo conf, int port) throws Exception {
		if(EtcdRegistry.port==0) EtcdRegistry.port = port;
		
		if(serverUuid ==null){
			serverUuid = StringUtils.isEmpty(ZebraRun.APP_NODE) ? UUID.randomUUID() + "" : ZebraRun.APP_NODE;
		}
		log.debug("begin register server to etcd3,RpcBaseConfig={}", conf);
		
		String key = "";
		if (ZebraConstants.TYPE_MONITOR.equals(conf.getType())) {
			key = ZebraConstants.ETCD_SERVICE_PRE + ZebraConstants.PATH_SEPARATOR + ZebraConstants.TYPE_MONITOR
					+ ZebraConstants.PATH_SEPARATOR + serverUuid;
			serviceName = ZebraConstants.TYPE_MONITOR;
		} else {
			if (ZebraConstants.TYPE_GATEWATY.equals(conf.getType())) {
				key = ZebraConstants.ETCD_SERVICE_PRE + ZebraConstants.PATH_SEPARATOR + conf.getGroup()
						+ ZebraConstants.PATH_SEPARATOR + ZebraConstants.TYPE_GATEWATY + ZebraConstants.PATH_SEPARATOR
						+ conf.getVersion() + ZebraConstants.PATH_SEPARATOR + conf.getSet()
						+ ZebraConstants.PATH_SEPARATOR + serverUuid;
				serviceName = ZebraConstants.TYPE_GATEWATY;
			} else {
				key = ZebraConstants.ETCD_SERVICE_PRE + ZebraConstants.PATH_SEPARATOR + conf.getGroup()
						+ ZebraConstants.PATH_SEPARATOR + conf.getService() + ZebraConstants.PATH_SEPARATOR
						+ conf.getVersion() + ZebraConstants.PATH_SEPARATOR + conf.getSet()
						+ ZebraConstants.PATH_SEPARATOR + conf.getType() + ZebraConstants.PATH_SEPARATOR + serverUuid;
				serviceName = conf.getService();
			}
		}
		String ip = NetUtils.getLocalHost();
		String value = ip + ":" + (port == 0 ? "" : port);
		Map<String, Object> keyValue = new HashMap<String, Object>();
		keyValue.put(ZebraConstants.KEY, key);
		keyValue.put(ZebraConstants.VALUE, value);
		log.debug("begin register server to etcd3,key={},value={}", key, value);
		getRegExecutor().execute(new EtcdPutRunnable(keyValue));
	}

	public synchronized void subscribe(Map<String, Object> subscribe, NotifyListener.NotifyServiceListener listener) {
		if (clientUuid == null) {
			clientUuid = StringUtils.isEmpty(ZebraRun.APP_NODE) ? UUID.randomUUID() + "" : ZebraRun.APP_NODE;
		}
		try {
			RpcServiceBaseInfo serviceInfo = new RpcServiceBaseInfo();
			serviceInfo.setGroup((String) subscribe.get(ZebraConstants.GROUP_KEY));
			serviceInfo.setService((String) subscribe.get(ZebraConstants.SERVICE_NAME));
			serviceInfo.setSet((String) subscribe.get(ZebraConstants.KEY_SET));
			serviceInfo.setVersion((String) subscribe.get(ZebraConstants.VERSION_KEY));
			// 开始去获取服务信息
			String key = ZebraConstants.ETCD_SERVICE_PRE + ZebraConstants.PATH_SEPARATOR + serviceInfo.getGroup()
					+ ZebraConstants.PATH_SEPARATOR + serviceInfo.getService() + ZebraConstants.PATH_SEPARATOR
					+ serviceInfo.getVersion() + ZebraConstants.PATH_SEPARATOR + serviceInfo.getSet()
					+ ZebraConstants.PATH_SEPARATOR + ZebraConstants.TYPE_SERVICE + ZebraConstants.PATH_SEPARATOR;
			Map<String, Object> keyValue = new HashMap<String, Object>();
			keyValue.put(ZebraConstants.KEY, key);
			List<KeyValue> ret = new ArrayList<>();
			//设置route信息
			if (!StringUtils.isEmpty((String) subscribe.get(ZebraConstants.ROUTE_KEY))
					|| !StringUtils.isEmpty((String) subscribe.get(ZebraConstants.KEY_ADDR))) {
				if (!StringUtils.isEmpty((String) subscribe.get(ZebraConstants.KEY_ADDR))) {
					keyValue.put(ZebraConstants.ROUTE_KEY, (String) subscribe.get(ZebraConstants.KEY_ADDR));
				} else {
					String route = (String) subscribe.get(ZebraConstants.ROUTE_KEY);
					if ("localhost".equals(route))
						route = NetUtils.getLocalHost();
					keyValue.put(ZebraConstants.ROUTE_KEY, route);
				}
			}
			EtcdClient.getSingleton().command(EtcdClient.GET_CMD, keyValue, ret, listener);
			GrpcURL url = new GrpcURL();
			url.setRemoteServiceAddrs(ret);
			serviceCache.put(serviceInfo, url);
			// 通知负载均衡器
			List<String> urls = new ArrayList<>();
			for (KeyValue value : ret) {
				urls.add(value.getValue().toStringUtf8());
			}
			listener.notify(serviceInfo, urls);
			// 注册client到消费者
			String customKey = ZebraConstants.ETCD_SERVICE_PRE + ZebraConstants.PATH_SEPARATOR + serviceInfo.getGroup()
					+ ZebraConstants.PATH_SEPARATOR + serviceInfo.getService() + ZebraConstants.PATH_SEPARATOR
					+ serviceInfo.getVersion() + ZebraConstants.PATH_SEPARATOR + serviceInfo.getSet()
					+ ZebraConstants.PATH_SEPARATOR + ZebraConstants.TYPE_CLIENT + ZebraConstants.PATH_SEPARATOR
					+ clientUuid;
			int port = 0;
			if (getGrpcProperties() == null) {
				port = PropertiesContent.getIntValue("zebra.grpc.port", 0);
			}
			String value = getServiceName() + ":" + NetUtils.getLocalHost() + ":"
					+ (port == 0 ? "" : port);
			String ttl = ZebraConstants.ZEBRA_ETCD_CUST_TTL;
			keyValue.put(ZebraConstants.KEY, customKey);
			keyValue.put(ZebraConstants.VALUE, value);
			keyValue.put(ZebraConstants.TTL, ttl);
			EtcdClient.getSingleton().command(EtcdClient.PUT_CMD, keyValue, null, null);
			// 添加监听器
			keyValue.put(ZebraConstants.KEY, key);
			EtcdClient.getSingleton().command(EtcdClient.WATCH_CMD, keyValue, null, listener);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.error("订阅服务失败，订阅信息={}", subscribe);
		}
	}

	public void refresh(Map<String, Object> subscribe, NotifyListener.NotifyServiceListener listener) {
		RpcServiceBaseInfo serviceInfo = new RpcServiceBaseInfo();
		serviceInfo.setGroup((String) subscribe.get(ZebraConstants.GROUP_KEY));
		serviceInfo.setService((String) subscribe.get(ZebraConstants.SERVICE_NAME));
		serviceInfo.setSet((String) subscribe.get(ZebraConstants.SERVICE_NAME));
		serviceInfo.setVersion((String) subscribe.get(ZebraConstants.VERSION_KEY));
		try {
			List<KeyValue> list = serviceCache.getIfPresent(serviceInfo).getRemoteServiceAddrs();
			// 通知负载均衡器
			List<String> urls = new ArrayList<>();
			for (KeyValue value : list) {
				urls.add(value.getValue().toStringUtf8());
			}
			listener.notify(serviceInfo, urls);
		} catch (Exception e) {

		}
	}

	/**
	 * @Title: register @Description:
	 * console、monitor、gateway、conf注册 @param @param type @param @throws
	 * Exception 设定文件 @return void 返回类型 @throws
	 */
	public void register(String name, String type, int port) throws Exception {
		EtcdRegistry registry = new EtcdRegistry();
		RpcServiceBaseInfo conf = new RpcServiceBaseInfo();
		conf.setGroup(ZebraConstants.DEFAULT_GROUP);
		conf.setSet(ZebraConstants.DEFAULT_SET);
		conf.setType(type);
		conf.setVersion(ZebraConstants.DEFAULT_VERSION);
		conf.setService(name);
		registry.register(conf, port);
	}

	public static void unRegister() {
		System.err.println("begin to unRegister!!!");
	}

	/**
	 * @Title: getServiceIps @Description: 获取服务所有的ip @param @param
	 * serviceName @param @return @param @throws Exception 设定文件 @return
	 * List<String> ip:port @throws
	 */
	public List<String> getServiceIps(String serviceName) throws Exception {
		Map<String, Object> keyValue = new HashMap<String, Object>();
		String key = "/";
		List<KeyValue> ret = Lists.newArrayList();
		boolean isService = false;
		if(ZebraConstants.ZEBRA_GATEWAY_NAME.equals(serviceName)){
			key = "/zebra/default/gateway/";
		}else if(ZebraConstants.ZEBRA_CONSOLE_NAME.equals(serviceName)){
			key = "/zebra/default/console/";
		}else if(ZebraConstants.ZEBRA_MONITOR_NAME.equals(serviceName)){
			key = "/zebra/monitor/";
		}else{
			isService = true;
		}
		keyValue.put(ZebraConstants.KEY, key);
		EtcdClient.getSingleton().command(EtcdClient.GET_CMD, keyValue, ret, null);
		if(isService){
			List<String> result = ret.stream()
					.filter(p -> p.getKey().toStringUtf8().split("/").length>=6&&p.getKey().toStringUtf8().split("/")[3].equals(serviceName)
							&& ZebraConstants.TYPE_SERVICE.equals(p.getKey().toStringUtf8().split("/")[6]))
					.map((KeyValue kv) -> {
						return kv.getValue().toStringUtf8();
					}).collect(Collectors.toList());
			return result;
		}
		List<String> result = ret.stream().map((KeyValue kv) -> {
					return kv.getValue().toStringUtf8();
				}).collect(Collectors.toList());
		return result;
	}

	/**
	 * @Title: getAllData @Description: 获取ETCD所有数据，并监听ETCD变化 @param @param
	 * listener 监听 @param @return @param @throws Exception 设定文件 @return
	 * List<KeyValue> 返回类型 @throws
	 */
	public List<KeyValue> getAllETCDData(MonitorNotifyListener.NotifyServiceListener listener) throws Exception {
		Map<String, Object> keyValue = new HashMap<String, Object>();
		String key = "/";
		keyValue.put(ZebraConstants.KEY, key);
		List<KeyValue> ret = Lists.newArrayList();
		EtcdClient.getSingleton().command(EtcdClient.GET_CMD, keyValue, ret, null);
		if(listener!=null){
			getRegExecutor().execute(new resolutionRunnable(keyValue, listener));
		}
		return ret;
	}
	
	/**
	 * @Title: getAllData @Description: 获取ETCD所有数据，并监听ETCD变化 @param @param
	 * listener 监听 @param @return @param @throws Exception 设定文件 @return
	 * List<KeyValue> 返回类型 @throws
	 */
	public List<KeyValue> getAllETCDData() throws Exception {
		Map<String, Object> keyValue = new HashMap<String, Object>();
		String key = "/";
		keyValue.put(ZebraConstants.KEY, key);
		List<KeyValue> ret = Lists.newArrayList();
		EtcdClient.getSingleton().command(EtcdClient.GET_CMD, keyValue, ret, null);
		return ret;
	}

	public GrpcProperties getGrpcProperties() {
		return grpcProperties;
	}

	public void setGrpcProperties(GrpcProperties grpcProperties) {
		this.grpcProperties = grpcProperties;
	}

	public Executor getRegExecutor() {
		if(regExecutor ==null){
			regExecutor = new ThreadPoolTaskExecutor();
			regExecutor.setBeanName(ZebraConstants.EXECUTOR_NAME_REG);
			regExecutor.setMaxPoolSize(5);
			regExecutor.setCorePoolSize(1);
			regExecutor.setQueueCapacity(1000);
			regExecutor.setDaemon(false);
			regExecutor.initialize();
		}
		return regExecutor;
	}

	private class resolutionRunnable implements Runnable {
		private Map<String, Object> keyValue;
		private MonitorNotifyListener.NotifyServiceListener listener;

		@Override
		public void run() {
			try {
				EtcdClient.getSingleton().monitorWatch(keyValue, listener);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public resolutionRunnable(Map<String, Object> keyValue, MonitorNotifyListener.NotifyServiceListener listener) {
			this.keyValue = keyValue;
			this.listener = listener;
		}
	};
	
	private class EtcdPutRunnable implements Runnable {
		private Map<String, Object> keyValue;
		@Override
		public void run() {
			try {
				int sleep = PropertiesContent.getIntValue(ZebraConstants.ZEBRA_LAZY_LOAD_TIME_KEY, 0);
				if (sleep > 0)
					Thread.sleep(sleep*1000);
				EtcdClient.getSingleton().command(EtcdClient.PUT_CMD, keyValue, null, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		public EtcdPutRunnable(Map<String, Object> keyValue) {
			this.keyValue = keyValue;
		}
	};
}
