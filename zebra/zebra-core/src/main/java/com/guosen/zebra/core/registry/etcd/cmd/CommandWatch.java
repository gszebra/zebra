package com.guosen.zebra.core.registry.etcd.cmd;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.Watch.Watcher;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.options.WatchOption;
import com.coreos.jetcd.watch.WatchEvent;
import com.coreos.jetcd.watch.WatchEvent.EventType;
import com.coreos.jetcd.watch.WatchResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;
import com.guosen.zebra.core.grpc.util.PropertiesContent;
import com.guosen.zebra.core.grpc.util.SpringContextUtils;
import com.guosen.zebra.core.registry.etcd.EtcdClient;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;
import com.guosen.zebra.core.registry.etcd.MonitorNotifyListener;
import com.guosen.zebra.core.registry.etcd.NotifyListener;
import com.guosen.zebra.core.route.Route;

public class CommandWatch {
	private static final Logger log = LogManager.getLogger(CommandWatch.class);
	private Set<String> watchlist = Sets.newConcurrentHashSet();
	private Set<String> monitorWatchList = Sets.newConcurrentHashSet();
	private Executor watchExecutor;

	public void watch(Map<String, Object> keyValue, NotifyListener.NotifyServiceListener listener) throws Exception {
		if(watchExecutor ==null){
			watchExecutor = SpringContextUtils.getBean("zebraBizExecutor");
		}
		Watcher watcher = null;
		try {
			String prefix = (String) keyValue.get(ZebraConstants.KEY);
			if (watchlist.contains(prefix))
				return;
			watchlist.add(prefix);
			watcher = EtcdClient.getSingleton().client.getWatchClient().watch(ByteSequence.fromString(prefix),
					WatchOption.newBuilder().withPrefix(ByteSequence.fromString(prefix)).build());
		} catch (Exception e) {
			throw e;
		}
		String route = (String) keyValue.get(ZebraConstants.ROUTE_KEY);
		while (true) {
			try {
				WatchResponse response = watcher.listen();
				for (WatchEvent event : response.getEvents()) {
					String key = event.getKeyValue().getKey().toStringUtf8();
					RpcServiceBaseInfo serviceInfo = getServiceInfo(key);
					List<KeyValue> urls = null;
					try {
						urls = EtcdRegistry.serviceCache.getIfPresent(serviceInfo).getRemoteServiceAddrs();
					} catch (Exception e) {

					}
					if (urls == null || urls.size() == 0 || event.getKeyValue() == null
							|| event.getKeyValue().getValue() == null)
						continue;
					List<String> lst = Lists.newArrayList();
					switch (event.getEventType()) {
					case PUT:
						if (!Route.isLegalRoute(route, event.getKeyValue()) && event.getKeyValue().getKey()
								.toStringUtf8().indexOf(EtcdRegistry.getServiceName()) > 0) {
							break;
						}
						urls.add(event.getKeyValue());
						Set<String> set = urls.stream().map((KeyValue kv) -> {
							return kv.getValue().toStringUtf8();
						}).collect(Collectors.toSet());
						if (set != null) {
							lst.addAll(set);
							watchExecutor.execute(new Runnable() {
								@Override
								public void run() {
									// 延迟加载,避免雪崩
									try {
										if (PropertiesContent.getIntValue(ZebraConstants.ZEBRA_LAZY_ROUTE, 0) != 0) {
											Thread.sleep(
													PropertiesContent.getIntValue(ZebraConstants.ZEBRA_LAZY_ROUTE, 0)
															* 1000);
										}
										listener.notify(serviceInfo, lst);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
						}
						break;
					case DELETE:
						set = urls.stream().filter(p -> !p.getKey().equals(event.getKeyValue().getKey()))
								.map((KeyValue kv) -> {
									return kv.getValue().toStringUtf8();
								}).collect(Collectors.toSet());

						lst.addAll(set);
						log.debug("etcd del event remainder addrs ={}", lst);
						listener.notify(serviceInfo, lst);
						break;
					default:
						log.error("unknow Event type from etcd");
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			Thread.sleep(5000);
		}
	}

	public void monitorWatch(Client client, Map<String, Object> keyValue,
			MonitorNotifyListener.NotifyServiceListener listener) throws Exception {
		Watcher watcher = null;
		try {
			String prefix = (String) keyValue.get(ZebraConstants.KEY);
			if (monitorWatchList.contains(prefix))
				return;
			monitorWatchList.add(prefix);
			log.debug("CommandWatch prefix={}", prefix);
			watcher = client.getWatchClient().watch(ByteSequence.fromString(prefix),
					WatchOption.newBuilder().withPrefix(ByteSequence.fromString(prefix)).build());
		} catch (Exception e) {
			if (watcher != null) {
				watcher.close();
			}
			throw e;
		}
		while (true) {
			try {
				if (listener == null)
					return;
				WatchResponse response = watcher.listen();
				for (WatchEvent event : response.getEvents()) {
					String key = event.getKeyValue().getKey().toStringUtf8();
					RpcServiceBaseInfo serviceInfo = getServiceInfo(key);
					if (ZebraConstants.TYPE_SERVICE.equals(serviceInfo.getType())
							|| ZebraConstants.TYPE_GATEWATY.equals(serviceInfo.getType())) {
						if (event.getEventType() != EventType.DELETE) {
							log.debug("CommandWatch del key ={},value={}", event.getKeyValue().getKey().toStringUtf8(),
									event.getKeyValue().getValue().toStringUtf8());
							serviceInfo.setPort(
									Integer.parseInt(event.getKeyValue().getValue().toStringUtf8().split(":")[1]));
							serviceInfo.setIp(event.getKeyValue().getValue().toStringUtf8().split(":")[0]);
						}
					}
					listener.notify(serviceInfo, event);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			Thread.sleep(5000);
		}

	}

	private RpcServiceBaseInfo getServiceInfo(String key) {
		RpcServiceBaseInfo serviceInfo = new RpcServiceBaseInfo();
		try {
			String args[] = key.split(ZebraConstants.PATH_SEPARATOR);
			String type = null;
			String fullServiceName = null;
			String group = null;
			String version = null;
			String set = null;
			String nodeId = null;
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
			} else {
				group = args[2];
				fullServiceName = args[3];
				version = args[4];
				set = args[5];
				nodeId = args[7];
			}
			serviceInfo.setGroup(group);
			serviceInfo.setVersion(version);
			serviceInfo.setSet(set);
			serviceInfo.setService(fullServiceName);
			serviceInfo.setNodeId(nodeId);
		} catch (Exception e) {

		}
		log.debug("getServiceInfo={}", serviceInfo);
		return serviceInfo;
	}
	
	public static void main(String args[]){
		String a ="/zebra/default/com.guosen.goldsun.service.GoldsunService/1.0.0/0/client/a460b5eb-7b04-4142-9596-d2e574ec8af5";
		String ars[] = a.split(ZebraConstants.PATH_SEPARATOR);
		System.err.println(ars.length);
		System.err.println(ars[6]);
	}
}