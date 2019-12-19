/**   
* @Title: RegistrySerivce.java 
* @Package com.guosen.zebra.console.service 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年11月22日 下午1:29:53 
* @version V1.0   
*/
package com.guosen.zebra.console.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coreos.jetcd.data.KeyValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guosen.App;
import com.guosen.zebra.console.dto.Counter;
import com.guosen.zebra.console.dto.Line;
import com.guosen.zebra.console.dto.RegitryBaseInfo;
import com.guosen.zebra.console.dto.ServiceApi;
import com.guosen.zebra.console.dto.ServiceDetail;
import com.guosen.zebra.console.dto.ServiceInfo;
import com.guosen.zebra.console.listener.SentinelListener;
import com.guosen.zebra.console.strategy.ServiceStrategyFactory;
import com.guosen.zebra.console.utils.PBResolver;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.anotation.ZebraConf;
import com.guosen.zebra.core.grpc.util.HttpUtils;
import com.guosen.zebra.core.monitor.health.Health;
import com.guosen.zebra.core.monitor.health.HealthGrpc;
import com.guosen.zebra.core.monitor.health.HealthGrpc.HealthBlockingStub;
import com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest;
import com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse;
import com.guosen.zebra.core.registry.etcd.EtcdClient;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;

import io.grpc.ManagedChannel;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.SharedResourceHolder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @ClassName: RegistrySerivce
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2017年11月22日 下午1:29:53
 * 
 */
@Service
public class ConsoleSerivce implements ApplicationListener<WebServerInitializedEvent> {

	private static final Logger log = LogManager.getLogger(ConsoleSerivce.class);

	@Autowired
	private EtcdRegistry etcdRegistry;

	private final OkHttpClient client = new OkHttpClient();

	@Value("${zebra.monitor.port:8083}")
	private int monitorPort;

	private ExecutorService executor = (ExecutorService) SharedResourceHolder.get(GrpcUtil.SHARED_CHANNEL_EXECUTOR);

	public static Map<String, Counter> serviceStatusCache = Maps.newHashMap();

	ZebraConf conf = App.class.getAnnotation(ZebraConf.class);

	public List<ServiceInfo> getAllApplication() {
		Map<String, ServiceInfo> serviceMap = Maps.newHashMap();
		List<ServiceInfo> list = Lists.newLinkedList();
		// 初始化坐标
		int x = 200;
		int y = 200;
		int i = 0;
		int max = 0;
		int rowTotal = 5;
		try {
			// 获取ETCD的所有kvs
			List<KeyValue> etcdRet = Lists.newArrayList();
			getAllEtcdData(etcdRet);
			// 每行数量根据返回的数量变化，避免图像往长条形发展
			int serverCount = etcdRet.size() / 4;
			if (serverCount > 30 && serverCount <= 50) {
				rowTotal = 8;
			} else if (serverCount > 50 && serverCount <= 100) {
				rowTotal = 10;
			} else if (serverCount > 100 && serverCount <= 200) {
				rowTotal = 15;
			} else if (serverCount > 200) {
				rowTotal = 20;
			}

			for (KeyValue kv : etcdRet) {
				ServiceInfo info = getServiceInfo(kv);
				if (serviceMap.get(info.getName()) == null) {
					serviceMap.put(info.getName(), info);
					if (!ZebraConstants.TYPE_MONITOR.equals(info.getType())
							&& !ZebraConstants.TYPE_GATEWATY.equals(info.getType())
							&& !ZebraConstants.TYPE_CONSOLE.equals(info.getType())
							&& !ZebraConstants.TYPE_CONF.equals(info.getType()) && !"console".equals(info.getName())
							&& !"conf".equals(info.getName()) && !"monitor".equals(info.getName())
							&& !"gateway".equals(info.getName())) {
						info.setX(x);
						info.setY(y);
						i++;
						x += 100;
						if ((i + 1) % rowTotal == 0) {
							x = 200;
							y += 50;
							i = 0;
						}
					}
				} else {
					String type = info.getType();
					info = serviceMap.get(info.getName());
					info.setType(type);
				}
				ServiceStrategyFactory.run(info, kv, serviceMap);

				if (x > max) {
					max = x;
				}
			}
			max += 100;
			if(serviceMap.get(ZebraConstants.TYPE_GATEWATY)!=null) {
				serviceMap.get(ZebraConstants.TYPE_GATEWATY).setX(max / 2 - 50);
			}
			for (String k : serviceStatusCache.keySet()) {
				String key = k;
				if (k.split("\\.").length > 0) {
					key = k.split("\\.")[k.split("\\.").length - 1];
				}
				ServiceInfo info = serviceMap.get(key);
				if (info == null) {
					info = new ServiceInfo();
					info.setName(key);
					info.setValue(k);
					info.setX(x);
					info.setY(y);
					i++;
					x += 100;
					if ((i + 1) % rowTotal == 0) {
						x = 200;
						y += 50;
						i = 0;
					}
				}
				info.setStatus(serviceStatusCache.get(k).getStatus());
				info.getAddrs().addAll(serviceStatusCache.get(k).getTotal());
				serviceMap.put(key, info);
			}
			getApiLines(serviceMap);
			list.addAll(serviceMap.values());
			list = list.stream().filter(p -> p.getX() != 0).collect(Collectors.toList());
			log.debug("ret list={}", list);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return list;
	}

	public ServiceApi getMethod(String serviceName, String ip, boolean needPb) throws InterruptedException {
		ServiceApi api = new ServiceApi();
		String targetId = ip;
		List<KeyValue> ret = Lists.newArrayList();
		ManagedChannel channel = null;
		try {
			ret = etcdRegistry.getAllETCDData();
			ret = ret.stream()
					.filter(item -> item.getKey().toStringUtf8().toLowerCase().contains(serviceName.toLowerCase()))
					.collect(Collectors.toList());
			for (KeyValue kv : ret) {
				String fullServiceName = kv.getKey().toStringUtf8();
				if (kv.getKey().toStringUtf8().split(ZebraConstants.PATH_SEPARATOR).length < 7)
					continue;
				fullServiceName = fullServiceName.split(ZebraConstants.PATH_SEPARATOR)[3];
				String type = kv.getKey().toStringUtf8().split(ZebraConstants.PATH_SEPARATOR)[6];
				if (fullServiceName.toLowerCase().contains(serviceName.toLowerCase())
						&& type.equals(ZebraConstants.TYPE_SERVICE)) {
					if (!StringUtils.isEmpty(ip)) {
						if (ip.equals(kv.getValue().toStringUtf8())) {
							targetId = kv.getValue().toStringUtf8();
							break;
						}
					} else {
						targetId = kv.getValue().toStringUtf8();
						api.getIps().add(kv.getValue().toStringUtf8());
					}
				} else {
					continue;
				}
				try {
					HealthRequest request = HealthRequest.newBuilder().setService(fullServiceName).build();
					Health health = new Health(null);
					if (targetId == null) {
						return api;
					}
					channel = (ManagedChannel) health.getChannel(targetId.split(":")[0],
							Integer.parseInt(targetId.split(":")[1]));
					HealthBlockingStub futureStub = HealthGrpc.newBlockingStub(channel);
					HealthResponse resp = futureStub.getMethods(request);
					for (int i = 0; i < resp.getMethodNameCount(); i++) {
						api.getServices().add(fullServiceName);
						api.getMethods().add(resp.getMethodName(i));
					}
					if (needPb) {
						String proto = resp.getMetrics().replace("\n", "!@#!");
						proto = proto.replace("\r", "!@#!");
						proto = proto.replace(" ", "@#");
						api.setProto(proto);
					}
					break;
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (channel != null) {
				channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
			}
		}
		return api;
	}

	public List<ServiceDetail> getMethod(String serviceName) throws InterruptedException {
		List<ServiceDetail> lists = Lists.newArrayList();
		ManagedChannel channel = null;
		try {
			List<KeyValue> ret = etcdRegistry.getAllETCDData();
			ret = ret.stream()
					.filter(item -> item.getKey().toStringUtf8().toLowerCase().contains(serviceName.toLowerCase()))
					.collect(Collectors.toList());
			List<String> alreadDealList = Lists.newArrayList();
			for (KeyValue kv : ret) {
				String fullServiceName = kv.getKey().toStringUtf8();
				if (kv.getKey().toStringUtf8().split(ZebraConstants.PATH_SEPARATOR).length < 7)
					continue;
				fullServiceName = fullServiceName.split(ZebraConstants.PATH_SEPARATOR)[3];
				if (alreadDealList.contains(fullServiceName)) {
					continue;
				}
				String type = kv.getKey().toStringUtf8().split(ZebraConstants.PATH_SEPARATOR)[6];
				String targetAddr = null;
				if (fullServiceName.toLowerCase().contains(serviceName.toLowerCase())
						&& type.equals(ZebraConstants.TYPE_SERVICE)) {
					targetAddr = kv.getValue().toStringUtf8();
				} else {
					continue;
				}
				try {
					HealthRequest request = HealthRequest.newBuilder().setService(fullServiceName).build();
					Health health = new Health(null);
					if (targetAddr == null) {
						return lists;
					}
					channel = (ManagedChannel) health.getChannel(targetAddr.split(":")[0],
							Integer.parseInt(targetAddr.split(":")[1]));
					HealthBlockingStub futureStub = HealthGrpc.newBlockingStub(channel);
					HealthResponse resp = futureStub.getMethods(request);
					JSONObject json = PBResolver.getPbResolver(resp.getMetrics());
					for (int i = 0; i < resp.getMethodNameCount(); i++) {
						Map<String, String> map = Maps.newHashMap();
						map.put("server", fullServiceName);
						map.put("method", resp.getMethodName(i));
						Request httpReq = new Request.Builder()
								.url(HttpUtils.getUrl(conf.confaddr() + "/zebra-conf/getServerTest", map)).build();
						String response = client.newCall(httpReq).execute().body().string();
						JSONObject result = JSON.parseObject(response);
						String desc = null;
						if (result.getJSONObject(ZebraConstants.KEY_DATA) != null) {
							desc = result.getJSONObject(ZebraConstants.KEY_DATA).getString("DESCRIPT");
						}
						
						JSONArray mds = json.getJSONArray("mds");
						JSONArray msgs = json.getJSONArray("msgs");

						ServiceDetail.Builder build = ServiceDetail.builder();
						for (Object obj : mds) {
							JSONObject md = (JSONObject) obj;
							if (resp.getMethodName(i).equals(md.getString("name"))) {
								if (!StringUtils.isEmpty(md.getString("commont"))) {
									desc = md.getString("commont");
								}
								for (Object msg : msgs) {
									if (((JSONObject) msg).getString("name").equals(md.getString("req"))
											|| ((JSONObject) msg).getString("name").equals(md.getString("rsp"))) {
										
										int j = 0;
										while (((JSONObject) msg).getJSONObject("field" + j) != null) {// 获取ref
											JSONObject field = ((JSONObject) msg).getJSONObject("field" + j);
											j++;
											if (!StringUtils.isEmpty(field.getString("ref"))) {
												String ref = field.getString("ref");
												if (ref.equals("com.guosen.zebra.dto.ResultDTO")) {
													JSONObject m = new JSONObject();
													m.put("name", "com.guosen.zebra.dto.ResultDTO");
													JSONObject f = new JSONObject();
													f.put("type", "string");
													f.put("option", "");
													f.put("name", "msg");
													m.put("field0", f);
													JSONObject f1 = new JSONObject();
													f1.put("type", "int32");
													f1.put("option", "");
													f1.put("name", "code");
													m.put("field1", f1);
													field.put("refObj", m);
													((JSONObject) msg).put("field" + (j-1), field);
													continue;
												}
												msgs.forEach(o -> {
													JSONObject m = (JSONObject) o;
													if (m.getString("name").equals(ref)) {
														field.put("refObj", m);
														
													}
												});
												((JSONObject) msg).put("field" + (j-1), field);
											}
										}
										if (((JSONObject) msg).getString("name").equals(md.getString("req"))) {
											build.withReq((JSONObject) msg);
										} else {
											build.withRsp((JSONObject) msg);
										}
									}
								}
							}
						}
						String proto = resp.getMetrics().replace("\r\n", "!@#!");
						proto = proto.replace(" ", "@#");

						ServiceDetail detail =build.withServerName(fullServiceName)
								.withMethod(resp.getMethodName(i)).withDesc(desc).build();
						lists.add(detail);
					}
					alreadDealList.add(fullServiceName);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					continue;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (channel != null) {
				channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
			}
		}
		return lists;
	}

	/**
	 * 注册zebra-gateway\zebra-cosole\zebra-monitor\zebra-conf到注册中心
	 */
	@Override
	public void onApplicationEvent(WebServerInitializedEvent event) {
		try {
			int port = event.getWebServer().getPort();
			etcdRegistry.register("console", ZebraConstants.TYPE_CONSOLE, port);
			etcdRegistry.getAllETCDData(new SentinelListener());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, List<String>> getAllService() {
		Map<String, List<String>> services = Maps.newHashMap();
		// 获取所有数据
		List<KeyValue> etcdRet = Lists.newArrayList();
		try {
			getAllEtcdData(etcdRet);
			for (KeyValue kv : etcdRet) {
				String val = kv.getValue().toStringUtf8();
				ServiceInfo info = getServiceInfo(kv);
				if (ZebraConstants.TYPE_SERVICE.equals(info.getType())) {
					List<String> ips = services.get(info.getValue());
					if (ips == null) {
						ips = Lists.newArrayList();
					}
					ips.add(val);
					services.put(info.getValue(), ips);
				}
			}
			log.debug("ret services={}", services);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return services;
	}

	@Scheduled(fixedRate = 60 * 1000)
	public List<JSONObject> monitorList() {
		List<JSONObject> list = Lists.newArrayList();
		try {
			Map<String, Object> keyValue = Maps.newHashMap();
			String key = ZebraConstants.ETCD_SERVICE_PRE + ZebraConstants.PATH_SEPARATOR + ZebraConstants.TYPE_MONITOR
					+ ZebraConstants.PATH_SEPARATOR;
			keyValue.put("key", key);
			List<KeyValue> retList = Lists.newArrayList();
			EtcdClient.getSingleton().command(EtcdClient.GET_CMD, keyValue, retList, null);
			if (retList.size() == 0)
				return list;
			String ip = retList.stream().map(p -> {
				return p.getValue().toStringUtf8();
			}).collect(Collectors.toList()).get(0).split(":")[0];
			String url = "http://" + ip + ":" + monitorPort + "/health";
			Request request = new Request.Builder().url(url).build();
			Response response = client.newCall(request).execute();
			String ret = response.body().string();
			try {
				if (response.isSuccessful()) {
					JSONArray array = JSONArray.parseArray(ret);
					list.addAll(array.toJavaList(JSONObject.class));
					list = list.stream().filter(item -> item != null).collect(Collectors.toList());
					// 计算服务状态
					CalServiceStatus cal = new CalServiceStatus(list);
					executor.execute(cal);
				} else {
					log.warn("monitor center response error {},url:{}", response.code(),url);
				}
			} catch (Exception e) {
				log.error(e.getMessage() + ",ret={}", ret);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return list;
	}

	private class CalServiceStatus implements Runnable {
		private List<JSONObject> list;

		public CalServiceStatus(List<JSONObject> list) {
			this.list = list;
		}

		public void run() {
			serviceStatusCache.clear();
			if (list == null || list.size() == 0)
				return;
			list.stream().forEach(item -> {
				try {
					String service = (String) item.get("gRpcService");
					String instance = (String) item.get("instance");
					int port = item.getIntValue("port");
					if (!StringUtils.isEmpty(service) && !StringUtils.isEmpty(instance)) {
						Counter c = serviceStatusCache.get(service);
						if (c == null) {
							c = new Counter();
							serviceStatusCache.put(service, c);
						}
						c.getTotal().add(instance + ":" + port);
						if ("DOWN".equals(item.get("status"))) {
							c.getDown().add(instance + ":" + port);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}

	private void getAllEtcdData(List<KeyValue> etcdRet) throws Exception {
		Map<String, Object> keyValue = new HashMap<String, Object>();
		String key = "/";
		keyValue.put("key", key);
		EtcdClient.getSingleton().command(EtcdClient.GET_CMD, keyValue, etcdRet, null);
	}

	private ServiceInfo getServiceInfo(KeyValue kv) {
		String fullServiceName = "";
		String type = "";
		String args[] = kv.getKey().toStringUtf8().split(ZebraConstants.PATH_SEPARATOR);
		fullServiceName = args[3];
		if (args.length == 8) {
			type = args[6];
		} else if (args.length == 7) {
			type = args[3];
		} else {
			type = args[2];
			fullServiceName = type;
		}
		String serviceName = fullServiceName;
		if (fullServiceName.split("\\.").length > 0) {
			serviceName = fullServiceName.split("\\.")[fullServiceName.split("\\.").length - 1];
		}
		ServiceInfo info = new ServiceInfo();
		info.setName(serviceName);
		info.setValue(fullServiceName);
		info.setType(type);
		if (ZebraConstants.TYPE_CLIENT.equals(info.getType()) || ZebraConstants.TYPE_SUB.equals(info.getType())) {
			String fromName = kv.getValue().toStringUtf8().split(":")[0];
			String fromShotName = fromName;
			if (fromName.split("\\.").length > 0) {
				fromShotName = fromName.split("\\.")[fromName.split("\\.").length - 1];
			}
			info.setName(fromShotName);
			info.setValue(fromName);
		}
		return info;
	}

	public List<RegitryBaseInfo> getZebraService() {
		List<RegitryBaseInfo> services = Lists.newArrayList();
		// 获取所有数据
		List<KeyValue> etcdRet = Lists.newArrayList();
		try {
			getAllEtcdData(etcdRet);
			for (KeyValue kv : etcdRet) {
				RegitryBaseInfo info = getFullServiceInfo(kv);
				if (!ZebraConstants.TYPE_CLIENT.equals(info.getType()))
					services.add(info);
			}
			log.debug("ret services={}", services);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return services;
	}

	private RegitryBaseInfo getFullServiceInfo(KeyValue kv) {
		RegitryBaseInfo serviceInfo = new RegitryBaseInfo();
		try {
			String key = kv.getKey().toStringUtf8();
			String val = kv.getValue().toStringUtf8();
			String args[] = key.split(ZebraConstants.PATH_SEPARATOR);
			String type = null;
			String fullServiceName = null;
			String group = null;
			String version = null;
			String set = null;
			String ip = null;
			String port = null;
			String nodeId = null;
			serviceInfo.setKey(key);
			serviceInfo.setLease(Long.toHexString(kv.getLease()));
			if (args.length == 8) {
				type = args[6];
			} else if (args.length == 7) {
				type = args[3];
			} else {
				type = args[2];
			}
			serviceInfo.setType(type);
			if (ZebraConstants.TYPE_CLIENT.equals(type))
				return serviceInfo;
			if (ZebraConstants.TYPE_GATEWATY.equals(type)) {
				fullServiceName = type;
				group = args[2];
				version = args[4];
				set = args[5];
				nodeId = args[6];
			} else if (ZebraConstants.TYPE_MONITOR.equals(type)) {
				fullServiceName = type;
				nodeId = args[3];
			} else {
				group = args[2];
				fullServiceName = args[3];
				version = args[4];
				set = args[5];
				nodeId = args[7];
			}
			ip = val.split(":")[0];
			port = val.split(":")[1];

			serviceInfo.setGroup(group);
			serviceInfo.setVersion(version);
			serviceInfo.setSet(set);
			serviceInfo.setService(fullServiceName);
			serviceInfo.setNodeId(nodeId);
			serviceInfo.setIp(ip);
			serviceInfo.setPort(port == null ? 0 : Integer.parseInt(port));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.debug("getServiceInfo={}", serviceInfo);
		return serviceInfo;
	}

	private void getApiLines(Map<String, ServiceInfo> serviceMap) throws IOException {
		ZebraConf conf = App.class.getAnnotation(ZebraConf.class);
		Request request = new Request.Builder().url(conf.confaddr() + "/zebra-conf/getGatewayConfNew").build();
		Response response = client.newCall(request).execute();
		String apis = response.body().string();
		JSONObject ret = JSON.parseObject(apis);
		JSONArray array = ret.getJSONArray(ZebraConstants.KEY_DATA);
		if(null!=array) {
			for (Object obj : array) {// 需要将网关访问的东西放在最前面，这样生产出来的东西会有层次感
				String fullServiceName = ((JSONObject) obj).getString("service");
				String serviceName = fullServiceName;
				if (fullServiceName.split("\\.").length > 0) {
					serviceName = fullServiceName.split("\\.")[fullServiceName.split("\\.").length - 1];
				}
				if (serviceMap.get(serviceName) != null) {
					Line line = new Line();
					line.setFrom(ZebraConstants.TYPE_GATEWATY);
					line.setTo(fullServiceName);
					serviceMap.get(ZebraConstants.TYPE_GATEWATY).getLines().add(line);
				}
			}
		}
	}
}
