package com.guosen.zebra.console.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.guosen.App;
import com.guosen.zebra.console.dto.MonitorDTO;
import com.guosen.zebra.console.dto.ServiceApi;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.grpc.anotation.ZebraConf;
import com.guosen.zebra.core.grpc.anotation.ZebraReference;
import com.guosen.zebra.core.grpc.server.GenericService;
import com.guosen.zebra.core.grpc.util.HttpUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;

@Service
public class ActiveProbingService {
	private static final Logger log = LogManager.getLogger(ActiveProbingService.class);
	@Autowired
	private ConsoleSerivce registrySerivce;
	@Autowired
	private ConsoleSerivce consoleSerivce;
	private Map<String, MonitorDTO> monitor = Maps.newHashMap();
	private final OkHttpClient client = new OkHttpClient();
	ZebraConf conf = (ZebraConf) App.class.getAnnotation(ZebraConf.class);
	@ZebraReference(timeOut = 600000)
	private GenericService genericService;
	@Value("${zebra.console.userCode:}")
	private String testUserCode;

	public Map<String, MonitorDTO> getMonitorResult() {
		return this.monitor;
	}

	@Scheduled(fixedRate = 600000L)
	public void methodTest() {
		Map<String, List<String>> services = this.registrySerivce.getAllService();
		services.forEach((k, v) -> {
			try {
				String ip = (String) v.get(0);
				ServiceApi api = this.consoleSerivce.getMethod(k, ip, false);
				List<String> methods = api.getMethods();
				for (String md : methods) {
					Map<String, String> map = Maps.newHashMap();
					map.put("server", k);
					map.put("method", md);
					Request request = (new Builder())
							.url(HttpUtils.getUrl(this.conf.confaddr() + "/zebra-conf/getServerTest", map)).build();
					String retStr = this.client.newCall(request).execute().body().string();
					if (StringUtils.isEmpty(retStr)) {
						MonitorDTO dtox = MonitorDTO.builder().withService(k).withAddr(ip).withMethod(md).withMessage("请求参数未配置")
								.withIsSuccess(false).withMonitorTime(new Date()).build();
						this.monitor.put(k + "/" + md + "/", dtox);
					} else {
						JSONObject obj = JSON.parseObject(retStr);
						JSONObject data = obj.getJSONObject("data");
						if(data ==null){
							MonitorDTO dtoxx = MonitorDTO.builder().withService(k).withAddr(ip).withMethod(md).withMessage("请求参数未配置")
									.withIsSuccess(false).withMonitorTime(new Date()).build();
							this.monitor.put(k + "/" + md + "/", dtoxx);
							continue;
						}
						JSONObject req = data.getJSONObject("REQUEST");
						String attach = data.getString("ATTACHMENTS");
						if (req == null) {
							MonitorDTO dtoxx = MonitorDTO.builder().withService(k).withAddr(ip).withMethod(md).withMessage("请求参数未配置")
									.withIsSuccess(false).withMonitorTime(new Date()).build();
							this.monitor.put(k + "/" + md + "/", dtoxx);
							continue;
						} 
						
						Map<String, String> attachMap = this.initAttatch(attach);
						RpcContext.getContext().setAttachments(attachMap);
						for (String addr : v) {
							long time = System.currentTimeMillis();
							try {
								JSONObject reply = this.genericService.$invoke(k, "default", "1.0.0", "0", addr, md,
										req);
								long delay = System.currentTimeMillis() - time;
								boolean isExpect = false;
								if (!StringUtils.isEmpty(obj.getString("RESPONSE"))) {
									JSONObject target = JSON.parseObject(obj.getString("RESPONSE"));
									Set<String> keys = target.keySet();
									for (String key : keys) {
										if (target.get(key) != null && !target.get(key).equals(reply.get(key))) {
											isExpect = false;
											break;
										}
									}

								} else {
									isExpect = false;
								}

								MonitorDTO dto = MonitorDTO.builder().withService(k).withAddr(ip).withMethod(md).withMessage("服务正常")
										.withIsSuccess(true).withIsExpect(isExpect).withMonitorTime(new Date())
										.withDesc(obj.getString("DESCRIPT")).withDelay(delay).build();
								this.monitor.put(k + "/" + md + "/" + addr, dto);
							} catch (Exception var27) {
								MonitorDTO dtoxxx = MonitorDTO.builder().withService(k).withAddr(ip).withMethod(md)
										.withMessage(var27.getMessage()).withIsSuccess(false).withIsExpect(false)
										.withMonitorTime(new Date()).withDesc(obj.getString("DESCRIPT")).withDelay(0L)
										.build();
								this.monitor.put(k + "/" + md + "/" + addr, dtoxxx);
							}
						}
					}
				}

				return;
			} catch (Exception var28) {
				log.error(var28.getMessage(), var28);
			}
		});
	}

	private Map<String, String> initAttatch(String attach) {
		Map<String, String> attachMap = Maps.newHashMap();
		if (!StringUtils.isEmpty(attach)) {
			attachMap = JSON.parseObject(attach, new TypeReference<Map<String, String>>() {
			});
		}
		return attachMap;
	}
}