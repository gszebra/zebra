package com.guosen.zebra.core.registry.etcd.cmd;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.coreos.jetcd.Lease;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.lease.LeaseGrantResponse;
import com.coreos.jetcd.lease.LeaseKeepAliveResponse;
import com.coreos.jetcd.options.PutOption;
import com.google.common.collect.Maps;
import com.guosen.zebra.core.common.NamedThreadFactory;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.registry.etcd.EtcdClient;
import com.guosen.zebra.core.registry.etcd.EtcdConstants;

public class CommandPut {
	private static final Logger log = LogManager.getLogger(CommandPut.class);
	// 定时刷新租约，保持服务持续正常
	private final ScheduledExecutorService freshExecutor = Executors
			.newSingleThreadScheduledExecutor(new NamedThreadFactory("freshLeaseTTL", false));
	private static Map<String,Map<String,Object>> existsLease =Maps.newConcurrentMap();
	
	public void put(Map<String, Object> param) throws Exception {
		Map<String, Object> keyValue = Maps.newHashMap(param);
		if (existsLease.containsKey((String) keyValue.get(ZebraConstants.KEY))) {// 避免重复创建定时任务
			return;
		}
		Lease lease = EtcdClient.getSingleton().client.getLeaseClient();
		LeaseGrantResponse resp = lease.grant(EtcdConstants.TTL).get();
		Long id = resp.getID();
		keyValue.put("leaseId", id);
		existsLease.put((String) keyValue.get(ZebraConstants.KEY), keyValue);
		PutOption option = PutOption.newBuilder().withLeaseId(resp.getID()).build();
		EtcdClient.getSingleton().client.getKVClient()
				.put(ByteSequence.fromString((String) keyValue.get(ZebraConstants.KEY)),
						ByteSequence.fromString((String) keyValue.get(ZebraConstants.VALUE)), option)
				.get();
		lease = null;
		// 定时刷新租约
		freshExecutor.scheduleAtFixedRate(new FixedRun((String) keyValue.get(ZebraConstants.KEY)), 0,
				EtcdConstants.TTL / 2, TimeUnit.SECONDS);
	}
	private class FixedRun implements Runnable{
		private final String key;
		public FixedRun(String key){
			this.key = key;
		}
		@Override
		public void run() {
			Long id = (Long) existsLease.get(key).get("leaseId");
			Lease newLease =null;
			try {
				newLease = EtcdClient.getSingleton().client.getLeaseClient();// 不使用lease防止内存泄露
				LeaseKeepAliveResponse resp = newLease.keepAliveOnce(id).get();
				if (resp !=null){
					newLease =null;
				}else{
					try{
						if(newLease!=null)newLease.revoke(id);
					}catch(Exception e){
						log.error("lease revoke exception:"+e.getMessage(), e);
					}
					refleshLease(key);
				}
			} catch (Exception t) { // 防御性容错,直接重新put一个KEY上去
				log.error("keepalive lease excpetion key {}, leaseid {}",key, id);
				try{
					if(newLease!=null)newLease.revoke(id);
				}catch(Exception e){
					log.error("lease revoke exception:"+e.getMessage(), e);
				}
				refleshLease(key);
			}
		}
		private void refleshLease(String key) {
			Lease lease = EtcdClient.getSingleton().client.getLeaseClient();
			try {
				LeaseGrantResponse resp = lease.grant(EtcdConstants.TTL).get();
				Long id = resp.getID();
				existsLease.get(key).put("leaseId", id);
				PutOption option = PutOption.newBuilder().withLeaseId(id).build();
				EtcdClient.getSingleton().client.getKVClient()
						.put(ByteSequence.fromString(key),
								ByteSequence.fromString((String) existsLease.get(key).get(ZebraConstants.VALUE)), option)
						.get();
			} catch (Exception e) {
				log.error("reflesh Lease exception:"+e.getMessage(),e);
			} 
			lease = null;
		}
	}

}
