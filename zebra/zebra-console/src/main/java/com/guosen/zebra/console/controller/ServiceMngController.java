package com.guosen.zebra.console.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guosen.App;
import com.guosen.zebra.console.dto.Result;
import com.guosen.zebra.console.dto.SentinelDTO;
import com.guosen.zebra.core.boot.autoconfigure.ZebraConfProp;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.anotation.ZebraConf;
import com.guosen.zebra.core.grpc.util.HttpUtils;
import com.guosen.zebra.core.monitor.health.Health;
import com.guosen.zebra.core.monitor.health.HealthGrpc;
import com.guosen.zebra.core.monitor.health.ServiceParam.SentinelRequest;
import com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;

import io.grpc.ManagedChannel;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@RestController
@RequestMapping(value = "/api/mng")
@EnableConfigurationProperties(ZebraConfProp.class)
public class ServiceMngController {
	private static final Logger log = LogManager.getLogger(ServiceMngController.class);
	
	ZebraConf conf = App.class.getAnnotation(ZebraConf.class);
	
	@Autowired
	private EtcdRegistry etcdRegistry;
	
	public static Map<String,SentinelRequest> errorMap = Maps.newConcurrentMap();
	
	private static Map<String,Integer> errorRetryTimes = Maps.newConcurrentMap();
	
	private final OkHttpClient client = new OkHttpClient();

	@RequestMapping(value = "qrySentinel", method = {RequestMethod.GET, RequestMethod.POST})
	public Result qrySentinel(int limit, int page, String serverName) {
		JSONObject result = new JSONObject();
		try {
			Map<String, String> param = Maps.newHashMap();
			param.put("serverName", serverName);
			Request request = new Request.Builder()
					.url(HttpUtils.getUrl(conf.confaddr() + "/zebra-conf/qrySentinel", param)).build();
			String retStr = client.newCall(request).execute().body().string();
			result = JSON.parseObject(retStr);
			
			JSONArray array = result.getJSONArray(ZebraConstants.KEY_DATA);
			List<JSONObject> ret = Lists.newArrayList();
			ret.addAll(array.toJavaList(JSONObject.class));
			int count = ret.size();
			if (!StringUtils.isEmpty(serverName)) {
				ret = ret.stream()
						.filter(obj -> obj.getString("serverName").contains(serverName))
						.collect(Collectors.toList());
			}
			ret = ret.stream().sorted(Comparator.comparing(obj -> ((JSONObject) obj).getString("serverName"))).collect(Collectors.toList());
			ret = ret.stream().skip((page - 1) * limit).limit(limit).collect(Collectors.toList());
			ret.stream().forEach(obj->{
				if(Integer.valueOf(obj.getString("type").trim()) ==1){
					obj.put("typeName", "限流");
                }else if(Integer.valueOf(obj.getString("type").trim()) ==2){
                	obj.put("typeName", "熔断");
                }else if(Integer.valueOf(obj.getString("type").trim()) ==3){
                	obj.put("typeName", "系统保护");
                }else if(Integer.valueOf(obj.getString("type").trim()) ==4){
                	obj.put("typeName", "白名单限制");
                }else if(Integer.valueOf(obj.getString("type").trim()) ==5){
                	obj.put("typeName", "黑名单限制");
                }else if(Integer.valueOf(obj.getString("type").trim()) ==6){
                	obj.put("typeName", "网关黑名单");
                }
			});
			
			return Result.builder().withCode(result.getIntValue(ZebraConstants.KEY_CODE))
					.withMsg(result.getString("msg")).withData(ret).withCount(count).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}
	
	@RequestMapping(value = "saveSentinel", method = {RequestMethod.GET, RequestMethod.POST})
	public Result saveSentinel(SentinelDTO param) {
		log.debug("saveSentinel param {}",param);
		JSONObject result = new JSONObject();
		try {
			Request request = new Request.Builder()
			        .url(HttpUtils.getUrl(conf.confaddr() + "/zebra-conf/saveSentinel", (JSONObject)JSON.toJSON(param)))
			        .build();
			String ret = client.newCall(request).execute().body().string();
			result = JSON.parseObject(ret);
			return Result.builder().withCode(result.getIntValue(ZebraConstants.KEY_CODE))
					.withMsg(result.getString("msg")).withData(result).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}
	
	@RequestMapping(value = "delSentinel", method = {RequestMethod.GET, RequestMethod.POST})
	public Result delSentinel(SentinelDTO param) {
		log.debug("delSentinel param {}",param);
		JSONObject result = new JSONObject();
		try {
			Request request = new Request.Builder()
			        .url(HttpUtils.getUrl(conf.confaddr() + "/zebra-conf/delSentinel", (JSONObject)JSON.toJSON(param)))
			        .build();
			String ret = client.newCall(request).execute().body().string();
			result = JSON.parseObject(ret);
			return Result.builder().withCode(result.getIntValue(ZebraConstants.KEY_CODE))
					.withMsg(result.getString("msg")).withData(result).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}
	
	
	@RequestMapping(value = "activeSentinel", method = {RequestMethod.GET, RequestMethod.POST})
	public Result activeSentinel(SentinelDTO param) {
		log.debug("activeSentinel param {}", param);
		try {
			List<String> ips = etcdRegistry.getServiceIps(param.getServerName());
			if (ips == null || ips.size() == 0) {
				return Result.builder().withCode(ZebraConstants.FAIL).withMsg("未找到该服务！").build();
			}
			for (String ip : ips) {
				ManagedChannel channel = null;
				try {
					Health health = new Health(null);
					channel = (ManagedChannel) health.getChannel(ip.split(":")[0], Integer.valueOf(ip.split(":")[1]));
					HealthGrpc.HealthBlockingStub blockStub = HealthGrpc.newBlockingStub(channel);
					SentinelRequest request = SentinelRequest.newBuilder().setType(param.getType().trim())
							.setData(param.getData()).build();
					if (!StringUtils.isEmpty(param.getIp())) {
						if (ip.split(":")[0].equals(param.getIp())) {
							SentinelResponse resp = blockStub.setSentinel(request);
							if (resp.getCode() == 0) {
								errorMap.put(ip, request);
							}
							return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("热加载成功").build();
						}
					} else {
						SentinelResponse resp = blockStub.setSentinel(request);
						if (resp.getCode() == 0) {
							errorMap.put(ip, request);
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				} finally {
					if (channel != null)
						channel.shutdownNow();
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
		return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("热加载成功").build();
	}
	
	@Scheduled(fixedRate = 3*60*1000)
	public void retrySentinel() {
		log.info("retry sentinel begins...{}", errorMap);
		errorMap.forEach((k, v) -> {
			if(!errorRetryTimes.containsKey(k)){
				errorRetryTimes.put(k, 0);
			}
			ManagedChannel channel = null;
			try {
				Health health = new Health(null);
				channel = (ManagedChannel) health.getChannel(k.split(":")[0], Integer.valueOf(k.split(":")[1]));
				HealthGrpc.HealthBlockingStub blockStub = HealthGrpc.newBlockingStub(channel);
				SentinelResponse resp = blockStub.setSentinel(v);
				if (resp.getCode() != 0) {
					errorMap.remove(k);
					errorRetryTimes.remove(k);
				}else{
					errorRetryTimes.put(k, errorRetryTimes.get(k)+1);
				}
				if(errorRetryTimes.get(k) >=3){
					errorRetryTimes.remove(k);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if (channel != null)
					channel.shutdownNow();
			}
		});
	}
}