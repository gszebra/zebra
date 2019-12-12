package com.guosen.zebra.core.registry.etcd;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.KeyValue;
import com.guosen.zebra.core.boot.autoconfigure.GrpcProperties;
import com.guosen.zebra.core.registry.etcd.cmd.CommandDel;
import com.guosen.zebra.core.registry.etcd.cmd.CommandGet;
import com.guosen.zebra.core.registry.etcd.cmd.CommandPut;
import com.guosen.zebra.core.registry.etcd.cmd.CommandWatch;
@Component
@ConditionalOnProperty(prefix = "zebra.grpc", value = "registryAddress")
public class EtcdClient {
	private static final Logger log = LogManager.getLogger(EtcdClient.class);
	private static EtcdClient singleton;

	public final static String GET_CMD = "get";
	public final static String DEL_CMD = "del";
	public final static String PUT_CMD = "put";
	public final static String WATCH_CMD = "watch";
	public final static String LEASE_CMD = "lease";

	@Autowired
	private GrpcProperties grpcProperties;
	CommandGet getCmd = new CommandGet();
	CommandPut putCmd = new CommandPut();
	CommandWatch watchCmd = new CommandWatch();
	CommandDel delCmd = new CommandDel();
	public Client client;

	public static EtcdClient getSingleton() {
		if (singleton == null) {
			synchronized (EtcdClient.class) {
				if (singleton == null) {
					singleton = new EtcdClient();
				}
			}
		}
		return singleton;
	}

	@PostConstruct
	public void init() {
		if (singleton == null) {
			singleton = getSingleton();
		}
		if (singleton.client == null) {
			log.info("begin init Etcd3 client,endpoints ={}", getGrpcProperties().getRegistryAddress());
			try {
				singleton.client = Client.builder().endpoints(getGrpcProperties().getRegistryAddress().split(","))
						.build();
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
				System.exit(0);
			}
			log.info("init Etcd3 client success");
		}
	}

	public void command(String parsedCmd, Map<String, Object> keyValue, List<KeyValue> ret,
			NotifyListener.NotifyServiceListener listener) throws Exception {
		if (client == null)
			Thread.sleep(3000);// 等待spring初始化完成，避免没有进行初始化
		try {
			switch (parsedCmd) {
			case GET_CMD:
				getCmd.get(keyValue, ret);
				break;
			case PUT_CMD:
				putCmd.put(keyValue);
				break;
			case WATCH_CMD:
				watchCmd.watch(keyValue, listener);
				break;
			case DEL_CMD:
				delCmd.del(keyValue);
				break;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	public void monitorWatch(Map<String, Object> keyValue, MonitorNotifyListener.NotifyServiceListener listener)
			throws Exception {
		try {
			watchCmd.monitorWatch(client, keyValue, listener);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	public GrpcProperties getGrpcProperties() {
		return grpcProperties;
	}

	public void setGrpcProperties(GrpcProperties grpcProperties) {
		this.grpcProperties = grpcProperties;
	}
}