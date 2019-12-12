/**   
* @Title: SentinelListener.java 
* @Package com.guosen.zebra.console.listener 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年12月4日 下午1:52:15 
* @version V1.0   
*/
package com.guosen.zebra.console.listener;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coreos.jetcd.watch.WatchEvent;
import com.coreos.jetcd.watch.WatchEvent.EventType;
import com.google.common.collect.Maps;
import com.guosen.App;
import com.guosen.zebra.console.controller.ServiceMngController;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;
import com.guosen.zebra.core.grpc.anotation.ZebraConf;
import com.guosen.zebra.core.grpc.util.HttpUtils;
import com.guosen.zebra.core.monitor.health.Health;
import com.guosen.zebra.core.monitor.health.HealthGrpc;
import com.guosen.zebra.core.monitor.health.ServiceParam.SentinelRequest;
import com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse;
import com.guosen.zebra.core.registry.etcd.MonitorNotifyListener;

import io.grpc.ManagedChannel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** 
* @ClassName: SentinelListener 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2018年12月4日 下午1:52:15 
*  
*/
public class SentinelListener implements MonitorNotifyListener.NotifyServiceListener{
	private static final Logger log = LogManager.getLogger(SentinelListener.class);
	
	ZebraConf conf = App.class.getAnnotation(ZebraConf.class);
	
	private final OkHttpClient client = new OkHttpClient();
	
	@Override
	public void notify(RpcServiceBaseInfo serviceInfo, WatchEvent event) {
		if(event.getEventType() == EventType.PUT){
			Map<String,String> param = Maps.newHashMap();
			param.put("serverName", serviceInfo.getService());
			if(ZebraConstants.TYPE_GATEWATY.equals(serviceInfo.getType())){
				param.put("serverName", ZebraConstants.ZEBRA_GATEWAY_NAME);
			}else if(ZebraConstants.TYPE_MONITOR.equals(serviceInfo.getType())){
				param.put("serverName", ZebraConstants.ZEBRA_MONITOR_NAME);
			}else if(ZebraConstants.TYPE_CONSOLE.equals(serviceInfo.getType())){
				param.put("serverName", ZebraConstants.ZEBRA_CONSOLE_NAME);
			}else if(ZebraConstants.TYPE_CLIENT.equals(serviceInfo.getType())){
				return;
			}
			try {
				Thread.sleep(3000);
				Request httpRequest = new Request.Builder()
				        .url(HttpUtils.getUrl(conf.confaddr() + "/zebra-conf/qrySentinel",param))
				        .build();
				Response response = client.newCall(httpRequest).execute();
				String ret = response.body().string();
				JSONObject result = JSON.parseObject(ret);
				log.debug("SentinelListener value ={}",event.getKeyValue().getValue().toStringUtf8());
				String ip = event.getKeyValue().getValue().toStringUtf8().split(":")[0];
				int port = Integer.valueOf(event.getKeyValue().getValue().toStringUtf8().split(":")[1]);

				ManagedChannel channel =null;
				try{
					Health health = new Health(null);
					JSONArray array = result.getJSONArray("data");
					if(array ==null ||array.size() == 0) return;
					channel = (ManagedChannel) health.getChannel(ip,port);
					HealthGrpc.HealthFutureStub stub = HealthGrpc.newFutureStub(channel);
					for(Object obj : array){
						JSONObject js = (JSONObject) obj;
						SentinelRequest request  = SentinelRequest.newBuilder().setType(js.getString("type")).setData(js.getString("data")).build();
						if(!StringUtils.isEmpty(js.getString("ip"))){
							if(ip.equals(js.getString("ip"))){
								SentinelResponse resp = stub.setSentinel(request).get();
								if(resp.getCode()==0){
									ServiceMngController.errorMap.put(ip+":"+port, request);
								}
							}
						}else{
							SentinelResponse resp = stub.setSentinel(request).get();
							if(resp.getCode()==0){
								ServiceMngController.errorMap.put(ip+":"+port, request);
							}
						}
					}
				}catch(Exception e){
					log.error(e.getMessage(),e);
				}finally{
					if(channel!=null) channel.shutdownNow();
				}
			
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
		}
	}
}
