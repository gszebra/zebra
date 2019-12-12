package com.guosen.zebra.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.watch.WatchEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guosen.zebra.admin.vo.MetricItem;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;
import com.guosen.zebra.core.monitor.health.Health;
import com.guosen.zebra.core.monitor.health.HealthGrpc;
import com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest;
import com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;
import com.guosen.zebra.core.registry.etcd.MonitorNotifyListener;
import com.guosen.zebra.core.serializer.utils.ZebraUtils;

import io.grpc.ManagedChannel;

@Service
public class MonitorService implements ApplicationListener<WebServerInitializedEvent> {
	private static final String METRIC_SVC = "com.guosen.zebra.core.monitor.health.Health";
	private static final String METRIC_METHOD = "getMetrics";
	static final String FULL_METRIC_SVC = METRIC_SVC + "." + METRIC_METHOD;

	private static final Logger logger = LogManager.getLogger(MonitorService.class);

	private static final int INTERVAL_SEC = 10;
	private static final Map<String, Map<String, Float>> lastCnt = Maps.newConcurrentMap();
	private static final Map<String, Map<String, MutableTriple<Float, Float, Float>>> lastLatency = Maps
			.newConcurrentMap();
	private static final Map<String, Map<String, MetricItem>> allMetrics = Maps.newConcurrentMap();
	private static final Map<String, Long> failMap = Maps.newConcurrentMap();
	private static final int MAX_FAIL_PERIOD = 3 * 24 * 60 * 60 * 1000;//3天
	private static Map<String, ManagedChannel> channels = Maps.newConcurrentMap();

	@Resource
	private EtcdRegistry etcdRegistry;
	private Map<String, Pair<String, Integer>> addrMap = Maps.newConcurrentMap();
	private MonitorNotifyListener.NotifyServiceListener listener = (RpcServiceBaseInfo rpcSeriviceBaseInfo,
			WatchEvent watchEvent) -> {

		logger.debug("recv event: [{}] {}", watchEvent.getEventType(), rpcSeriviceBaseInfo);

		final String svcType = rpcSeriviceBaseInfo.getType();
		if (!(ZebraConstants.TYPE_SERVICE.equals(svcType) || ZebraConstants.TYPE_GATEWATY.equals(svcType))) {
			return;
		}

		if (WatchEvent.EventType.PUT == watchEvent.getEventType()) {
			final String ip = rpcSeriviceBaseInfo.getIp();
			final Integer port = Integer.valueOf(rpcSeriviceBaseInfo.getPort());
			if (ip == null || port == 0)
				return;
			Pair<String, Integer> addrItem = ImmutablePair.of(ip, port);
			addrMap.put(rpcSeriviceBaseInfo.getNodeId(), addrItem);

			// addInitMetricItem(rpcSeriviceBaseInfo.getNodeId(), ip, port);
		} else if (WatchEvent.EventType.DELETE == watchEvent.getEventType()) {
			// mark unreachable instead of removing it.
			checkAvailable(rpcSeriviceBaseInfo.getNodeId());
		}
	};

	private void checkAvailable(String nodeId) {
		if (StringUtils.isEmpty(nodeId)) {
			return;
		}
		Long createdTime = failMap.get(nodeId);
		logger.debug("checkAvailable nodeId ={},createdTime={},item={}",nodeId,createdTime, allMetrics.get(nodeId));
		if (null == createdTime||createdTime ==0) {
			Map<String, MetricItem> methodMetricMap = allMetrics.get(nodeId);
			if (null == methodMetricMap || methodMetricMap.isEmpty()) {
				return;
			}
			failMap.put(nodeId, System.currentTimeMillis());
			for (MetricItem mi : methodMetricMap.values()) {
				mi.setStatus(MetricItem.STAT_DOWN);
			}
			logger.debug("nodeId ={} is error,methodMetricMap ={}", nodeId, methodMetricMap);
		} else if (System.currentTimeMillis() - createdTime >= MAX_FAIL_PERIOD) {
			logger.debug("Delete key = {}", nodeId);
			allMetrics.remove(nodeId);
			addrMap.remove(nodeId);
			lastCnt.remove(nodeId);
			lastLatency.remove(nodeId);
			failMap.remove(nodeId);
			try {
				channels.remove(nodeId).shutdown();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		failMap.forEach((k,v)->{
			logger.debug("failMap nodeId= {},duration={},service ={}", k,(System.currentTimeMillis() - v)/1000,allMetrics.get(k));
		});
		

	}

	// Using @PostConstruct will encounter a problem that EtcdClient is
	// initialized after MonitorService
	// @PostConstruct
	private void init() {
		List<KeyValue> etcdDataList = Collections.emptyList();
		try {
			etcdDataList = etcdRegistry.getAllETCDData(listener);
		} catch (Exception e) {
			logger.error("Initialization failed when calling getAllETCDData()", e);
		}
		logger.debug("etcdDataList size={}", etcdDataList.size());
		for (KeyValue kv : etcdDataList) {
			String etcdKey = kv.getKey().toStringUtf8();
			logger.debug("etcdKey={}", etcdKey);
			RpcServiceBaseInfo svcInfo = ZebraUtils.getServiceInfo(etcdKey);
			String svcType = svcInfo.getType();
			if (ZebraConstants.TYPE_SERVICE.equals(svcType) || ZebraConstants.TYPE_GATEWATY.equals(svcType)) {
				String etcdVal = kv.getValue().toStringUtf8();
				String[] addrPart = etcdVal.split(":");
				if (addrPart.length >= 2) {
					String ip = addrPart[0];
					Integer port = Integer.valueOf(addrPart[1]);

					// init addrMap
					// String grpcSvc = svcInfo.getService();
					Pair<String, Integer> addrItem = ImmutablePair.of(ip, port);
					addrMap.put(svcInfo.getNodeId(), addrItem);

					// init allMetrics
					// addInitMetricItem(svcInfo.getNodeId(), ip, port);
				}
			}
		}
	}

	// private void addInitMetricItem(String nodeId, String ip, Integer port) {
	// MetricItem mi = new MetricItem();
	// mi.setInstance(ip);
	// //mi.setPort(port);
	// //No method info in etcd, use "Health.getMetrics" as default service and
	// method
	// mi.setgRpcService(METRIC_SVC);
	// mi.setgRpcMethod(METRIC_METHOD);
	// Map<String, MetricItem> m = Maps.newHashMapWithExpectedSize(1);
	// m.put(METRIC_METHOD, mi);
	// allMetrics.put(nodeId, m);
	// }

	public String fetchAllNodeMetrics() {
		HealthRequest request = HealthRequest.newBuilder().setService(FULL_METRIC_SVC).build();
		Health health = new Health(null);
		// TODO use parallel streaming for concurrency.
		StringBuilder sb = new StringBuilder();
		logger.info("get all metrics from {}", addrMap.values());
		for (String key : addrMap.keySet()) {
			Pair<String, Integer> p = addrMap.get(key);
			if (p == null || p.getLeft() == null || p.getRight() == null)
				continue;
			ManagedChannel channel = null;
			try {
				long start = System.currentTimeMillis();
				channel = channels.get(key);
				if (channel == null || channel.isShutdown()) {
					channel = (ManagedChannel) health.getChannel(p.getLeft(), p.getRight().intValue());
					channels.put(key, channel);
				}
				logger.debug("get metrics from {} getChannel take time{}", p, System.currentTimeMillis() - start);
				HealthGrpc.HealthBlockingStub futureStub = HealthGrpc.newBlockingStub(channel);
				logger.debug("get metrics from {} newBlockingStub take time{}", p, System.currentTimeMillis() - start);
				HealthResponse resp = futureStub.getMetrics(request);
				logger.debug("get metrics from {} getMetrics take time{}", p, System.currentTimeMillis() - start);
				sb.append(resp.getMetrics());
				if (failMap.containsKey(key)) {
					failMap.remove(key);
					Map<String, MetricItem> methodMetricMap = allMetrics.get(key);
					for (MetricItem mi : methodMetricMap.values()) {
						mi.setStatus(MetricItem.STAT_SERVING);
					}
				}
				logger.info("get metrics from {} take time total{}", p, System.currentTimeMillis() - start);
			} catch (Exception e) {
				
				logger.error(e.getMessage(), e);
				checkAvailable(key);
			}
		}
		sb.append(getStatMetricsString());
		return sb.toString();
	}

	public List<MetricItem> getCollectedMetrics() {
		List<MetricItem> retList = Lists.newArrayList();
		for(String key :addrMap.keySet()){
			Map<String, MetricItem> item = allMetrics.get(key);
			if(item == null) continue;
			if(item.values() == null) continue;
			retList.addAll(item.values());
		}
		return retList;
	}

	// refresh every 10s.
	@Scheduled(fixedRateString = "${refresh_interval_ms}")
	public void refreshMetrics() {
		logger.debug("Collecting metrics begins...");
		HealthRequest request = HealthRequest.newBuilder().setService(FULL_METRIC_SVC).build();
		Health health = new Health(null);
		HealthResponse resp = null;
		for (Map.Entry<String, Pair<String, Integer>> e : addrMap.entrySet()) {
			String nodeId = e.getKey();
			Pair<String, Integer> p = e.getValue();
			logger.debug("get metrics from {}", p);
			if (p == null || p.getLeft() == null || p.getRight() == null)
				continue;
			final String ip = p.getLeft();
			final int port = p.getRight();
			if (StringUtils.isEmpty(ip) || 0 == port) {
				continue;
			}
			ManagedChannel channel = null;
			try {
				channel = channels.get(nodeId);
				if (channel == null || channel.isShutdown()) {
					channel = (ManagedChannel) health.getChannel(ip, port);
					channels.put(nodeId, channel);
				}
				HealthGrpc.HealthBlockingStub futureStub = HealthGrpc.newBlockingStub(channel);
				resp = futureStub.getMetrics(request);
				if (failMap.containsKey(nodeId)) {
					failMap.remove(nodeId);
					Map<String, MetricItem> methodMetricMap = allMetrics.get(nodeId);
					for (MetricItem mi : methodMetricMap.values()) {
						mi.setStatus(MetricItem.STAT_SERVING);
					}
				}
				processNodeMetrics(nodeId, ip, port, resp);
			} catch (Exception ex) {
				logger.error("Calling getMetrics of " + p + " throws ex:", ex);
				// allMetrics.remove(nodeId);
				// mark unreachable
				checkAvailable(nodeId);
				// lastLatency.remove(nodeId);
				// lastCnt.remove(nodeId);
			}
		}
	}

//	@Scheduled(fixedRate = 1000*60)
//	public void removeErrorData() {
//		for(String key : allMetrics.keySet()){
//			Map<String, MetricItem> value = allMetrics.get(key);
//			boolean isErrorData = false;
//			for (String itemKey : value.keySet()) {
//				MetricItem mi = value.get(itemKey);
//				if(mi.getStatus().equals(MetricItem.STAT_DOWN)&& !failMap.containsKey(key)){
//					isErrorData = true;
//					break;
//				}
//			}
//			if(isErrorData){
//				logger.info("delete key from allMetrics key={}, value = {}",key,value);
//				allMetrics.remove(key);
//			}
//		}
//	}

	private void processNodeMetrics(String nodeId, String ip, int port, HealthResponse resp) {
		if (null == resp) {
			return;
		}
		String metricStr = StringUtils.trim(resp.getMetrics());
		String line = null;
		try (BufferedReader br = new BufferedReader(new StringReader(metricStr))) {
			while ((line = br.readLine()) != null) {
				byte cmd = 0;
				if (line.startsWith("grpc_server_started_total")) {
					cmd = 1;
				} else if (line.startsWith("grpc_server_handling_ms_count")) {
					cmd = 2;
				} else if (line.startsWith("grpc_server_handling_ms_sum")) {
					cmd = 3;
				}
				if (1 == cmd || 2 == cmd || 3 == cmd) { // TODO extract strategy
					final int preLabelInx = line.indexOf('{');
					// String metricName = line.substring(0, preLabelInx);
					final int postLabelInx = line.lastIndexOf('}');
					String labelStr = line.substring(preLabelInx + 1, postLabelInx);
					String val = line.substring(postLabelInx + 2);
					float currMetricVal = Float.parseFloat(val);

					String key = nodeId;// labelStr;
					String grpcMethod = extractFromMetricLabels(labelStr, "grpc_method");
					if (1 == cmd) {

						Map<String, Float> lastCntOfNode = lastCnt.get(key);
						if (null == lastCntOfNode) {
							lastCntOfNode = Maps.newHashMapWithExpectedSize(32);
						}
						Float lastMetricVal = lastCntOfNode.get(grpcMethod);
						lastCntOfNode.put(grpcMethod, currMetricVal);
						lastCnt.put(key, lastCntOfNode); // TODO chk reference
															// changes.
						if (null != lastMetricVal) {
							// int qps =
							// Math.toIntExact(Math.round(abs(currMetricVal -
							// lastMetricVal) / INTERVAL_SEC));
							float qps = Math.abs(currMetricVal - lastMetricVal) / INTERVAL_SEC;
							// update final metrics
							Map<String, MetricItem> methodMetricMap = allMetrics.getOrDefault(key,
									Maps.newHashMapWithExpectedSize(32));
							MetricItem mItm = methodMetricMap.get(grpcMethod);
							if (null != mItm) {
								mItm.setQps(qps);
								mItm.setStatus(MetricItem.STAT_SERVING);
								mItm.setPort(port);
								// TODO check if needed.
								methodMetricMap.put(grpcMethod, mItm);
								allMetrics.put(key, methodMetricMap);
							} else {
								mItm = new MetricItem(labelStr, port);
								methodMetricMap.put(grpcMethod, mItm);
								allMetrics.put(key, methodMetricMap);
							}
						}
					} else if (2 == cmd) {
						// upd count
						Map<String, MutableTriple<Float, Float, Float>> lastLatencyOfNode = lastLatency.get(key);
						if (null == lastLatencyOfNode) {
							lastLatencyOfNode = Maps.newHashMapWithExpectedSize(32);
						}
						MutableTriple<Float, Float, Float> lastMetricTri = lastLatencyOfNode.get(grpcMethod);
						if (null != lastMetricTri) {
							// upd lastCnt; upd dCnt;
							Float lastCnt = lastMetricTri.getLeft();
							// Float dCnt = lastMetricTri.getMiddle();
							lastMetricTri.setMiddle(Math.abs(currMetricVal - lastCnt));
							lastMetricTri.setLeft(currMetricVal);
							lastLatencyOfNode.put(grpcMethod, lastMetricTri);
							lastLatency.put(key, lastLatencyOfNode);
						} else {
							lastLatencyOfNode.put(grpcMethod,
									MutableTriple.of(Float.valueOf(0.0f), Float.valueOf(1.0f), Float.valueOf(0.0f)));
							lastLatency.put(key, lastLatencyOfNode);
						}
					} else if (3 == cmd) {
						// avgLatency = dSum/dCnt;
						Map<String, MutableTriple<Float, Float, Float>> lastLatencyOfNode = lastLatency.get(key);
						if (null == lastLatencyOfNode) {
							lastLatencyOfNode = Maps.newHashMapWithExpectedSize(32);
						}
						MutableTriple<Float, Float, Float> lastMetricTri = lastLatencyOfNode.get(grpcMethod);
						if (null != lastMetricTri) {
							int avgLatency = Math.round(
									Math.abs(currMetricVal - lastMetricTri.getRight()) / lastMetricTri.getMiddle());
							lastMetricTri.setRight(currMetricVal);
							lastLatencyOfNode.put(grpcMethod, lastMetricTri);
							lastLatency.put(key, lastLatencyOfNode);
							Map<String, MetricItem> nodeMetricMap = allMetrics.getOrDefault(key,
									Maps.newHashMapWithExpectedSize(32));
							MetricItem mItm = nodeMetricMap.get(grpcMethod);
							if (null != mItm) {
								mItm.setPort(port);
								if ("READY".equals(mItm.getStatus())) {
									mItm.buildByLabelStr(labelStr);
								}
								mItm.setAvgLatency(Integer.valueOf(avgLatency));
								nodeMetricMap.put(grpcMethod, mItm);
								allMetrics.put(key, nodeMetricMap);
							} else {
								mItm = new MetricItem(labelStr, port);
								nodeMetricMap.put(grpcMethod, mItm);
								allMetrics.put(key, nodeMetricMap);
							}
						} else {
							lastLatencyOfNode.put(grpcMethod,
									MutableTriple.of(Float.valueOf(0.0f), Float.valueOf(1.0f), Float.valueOf(0.0f)));
							lastLatency.put(key, lastLatencyOfNode);
						}

					}

				}

			}
		} catch (IOException e) {
			logger.error("ex when reading metrics line after " + line, e);
		}

	}

	public String extractFromMetricLabels(String labelStr, String grpc_method) {
		if (StringUtils.isEmpty(labelStr) || StringUtils.isEmpty(grpc_method)) {
			return "";
		}
		// 26.0
		int inx1 = labelStr.indexOf(grpc_method);
		inx1 = labelStr.indexOf("=", inx1) + 2;
		int inx2 = labelStr.indexOf('\"', inx1); // TODO chk bound

		return labelStr.substring(inx1, inx2);
	}

	public String getStatMetricsString() {
		StringBuilder sb = new StringBuilder();
		sb.append("# HELP ").append("server_state").append(" ").append("The state of service.").append("\n");
		sb.append("# TYPE ").append("server_state").append(" ").append("gauges").append("\n");
		allMetrics.keySet().forEach(key -> {
			Map<String, MetricItem> map = allMetrics.get(key);
			MetricItem item = (MetricItem) map.values().toArray()[0];
			sb.append("server_state").append('{');
			sb.append("grpc_service");
			sb.append("=\"");
			sb.append(item.getgRpcService());
			sb.append("\"");
			sb.append(",");
			sb.append("instance");
			sb.append("=\"");
			sb.append(item.getInstance());
			sb.append("\"");
			sb.append(",");
			sb.append("port");
			sb.append("=\"");
			sb.append(item.getPort());
			sb.append("\"");
			sb.append('}');
			sb.append(' ');
			int status = (MetricItem.STAT_DOWN.equals(item.getStatus())) ? 0 : 1;
			sb.append(status);
			sb.append("\n");
		});
		sb.append("\n\n");
		return sb.toString();
	}

	@Override
	public void onApplicationEvent(WebServerInitializedEvent arg0) {
		init();
	}
}
