package com.guosen.zebra.console.controller;

import java.net.URLEncoder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.guosen.zebra.console.utils.ConContrant;
import com.guosen.zebra.core.boot.autoconfigure.GrpcProperties;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.anotation.ZebraConf;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

@RestController
@RequestMapping(value = "/api/conf")
public class ConfCenterController {
	private static final Logger log = LogManager.getLogger(ConfCenterController.class);

	private ZebraConf conf = App.class.getAnnotation(ZebraConf.class);

	@Autowired
	private GrpcProperties grpcProperties;

	private final OkHttpClient client = new OkHttpClient();

	@RequestMapping(value = "gatewayConflist", method = { RequestMethod.GET, RequestMethod.POST })
	public Result gatewayConflist(int limit, int page, String server) {
		List<JSONObject> list = Lists.newArrayList();
		try {
			Request request = new Request.Builder().url(conf.confaddr() + "/zebra-conf/getGatewayConf").build();
			String ret = client.newCall(request).execute().body().string();
			JSONObject json = JSON.parseObject(ret);
			if (json.getIntValue(ZebraConstants.KEY_CODE) == ZebraConstants.FAIL)
				return Result.builder().withCode(ZebraConstants.FAIL).withMsg(json.getString("msg")).withData(list)
						.build();
			JSONArray array = json.getJSONArray(ZebraConstants.KEY_DATA);
			list.addAll(array.toJavaList(JSONObject.class));
			if (!StringUtils.isEmpty(server)) {
				list = list.stream().filter(obj -> obj.getString("service").contains(server))
						.collect(Collectors.toList());
			}
			int count = list.size();
			list = list.stream().sorted(Comparator.comparing(obj -> ((JSONObject) obj).getString("service")))
					.collect(Collectors.toList());
			list = list.stream().skip((page - 1) * limit).limit(limit).collect(Collectors.toList());

			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("查询成功").withData(list).withCount(count)
					.build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").withData(list).build();
		}
	}

	@RequestMapping(value = "updateGatewayConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result updateGatewayConf(String sid, String service, String group, String version, String set, String path, String text,
			String tag, String gatewaySet) {
		try {
			String url = "/zebra-conf/updGatewayConf";
			if(StringUtils.isEmpty(sid)){
				url="/zebra-conf/insertGatewayConf";
			}
			Map<String, String> map = Maps.newConcurrentMap();
			map.put("sid", sid);
			map.put("service", service);
			map.put("group", group);
			map.put("version", version);
			map.put("set", set);
			map.put("path", path);
			map.put("text", URLEncoder.encode(text, "utf-8"));
			map.put("tag", URLEncoder.encode(tag, "utf-8"));
			map.put("optType", "update");
			map.put("gatewaySet", gatewaySet);
			RequestBody body = RequestBody.create(ConContrant.jsonType, JSON.toJSONString(map));
			Request request = new Request.Builder().url(conf.confaddr() + url).post(body)
					.build();
			String ret = client.newCall(request).execute().body().string();
			log.debug("ret={}", ret);
			JSONObject json = JSON.parseObject(ret);
			return Result.builder().withCode(json.getIntValue(ZebraConstants.KEY_CODE)).withMsg(json.getString("msg"))
					.build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "deleteGatewayConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result deleteGatewayConf(int sid, String service) {
		try {
			Map<String, String> map = Maps.newConcurrentMap();
			map.put("sid", sid + "");
			map.put("optType", "delete");
			map.put("service", service);
			RequestBody body = RequestBody.create(ConContrant.jsonType, JSON.toJSONString(map));

			Request request = new Request.Builder().url(conf.confaddr() + "/zebra-conf/delGatewayConf").post(body)
					.build();
			String ret = client.newCall(request).execute().body().string();
			log.debug("ret={}", ret);
			JSONObject json = JSON.parseObject(ret);
			return Result.builder().withCode(json.getIntValue(ZebraConstants.KEY_CODE)).withMsg(json.getString("msg"))
					.build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "listConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result listConf(int limit, int page, String server, int type) {
		List<JSONObject> list = Lists.newArrayList();
		try {
			okhttp3.FormBody.Builder build = new FormBody.Builder().add("type", type + "");
			RequestBody body = build.build();
			Request request = new Request.Builder().url(conf.confaddr() + "/zebra-conf/getConfNew").post(body).build();
			String ret = client.newCall(request).execute().body().string();
			JSONObject json = JSON.parseObject(ret);
			JSONArray array = json.getJSONArray(ZebraConstants.KEY_DATA);
			list.addAll(array.toJavaList(JSONObject.class));

			if (!StringUtils.isEmpty(server)) {
				list = list.stream().filter(obj -> obj.getString("server").contains(server))
						.collect(Collectors.toList());
			}
			int count = list.size();
			list = list.stream().sorted(Comparator.comparing(obj -> ((JSONObject) obj).getString("server")))
					.collect(Collectors.toList());
			list = list.stream().skip((page - 1) * limit).limit(limit).collect(Collectors.toList());

			return Result.builder().withCode(json.getIntValue(ZebraConstants.KEY_CODE)).withMsg(json.getString("msg"))
					.withCount(count).withData(list).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "updateConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result updateConf(String sid, String type, String server, String scope, String scopeName, String text) {
		try {
			if (StringUtils.isEmpty(sid)) {
				sid = "0";
			}
			Map<String, String> map = Maps.newConcurrentMap();
			map.put("sid", sid);
			map.put("server", server);
			map.put("type", type);
			map.put("scope", scope);
			map.put("scopeName", scopeName);
			map.put("text", URLEncoder.encode(text, "utf-8"));
			String jsonString = JSON.toJSONString(map);
			RequestBody body = RequestBody.create(ConContrant.jsonType, jsonString);
			Request request = new Request.Builder().post(body).url(conf.confaddr() + "/zebra-conf/updConf").build();
			String ret = client.newCall(request).execute().body().string();
			log.debug("ret={}", ret);
			JSONObject json = JSON.parseObject(ret);
			return Result.builder().withCode(json.getIntValue(ZebraConstants.KEY_CODE)).withMsg(json.getString("msg"))
					.build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "delConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result delConf(String sid) {
		try {
			if (StringUtils.isEmpty(sid)) {
				sid = "0";
			}
			RequestBody body = new FormBody.Builder().add("sid", sid + "").build();
			Request request = new Request.Builder().url(conf.confaddr() + "/zebra-conf/delConf").post(body).build();
			String ret = client.newCall(request).execute().body().string();
			log.debug("ret={}", ret);
			JSONObject json = JSON.parseObject(ret);
			return Result.builder().withCode(json.getIntValue(ZebraConstants.KEY_CODE)).withMsg(json.getString("msg"))
					.build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "getRegistry", method = { RequestMethod.GET, RequestMethod.POST })
	public String getRegistry() {
		return grpcProperties.getRegistryAddress();
	}

	@RequestMapping(value = "listHisConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result listHisConf(int limit, int page, String sid) {
		List<JSONObject> list = Lists.newArrayList();
		try {
			RequestBody body = new FormBody.Builder().add("sid", sid).build();
			Request request = new Request.Builder().url(conf.confaddr() + "/zebra-conf/qryConfHistory").post(body)
					.build();
			String ret = client.newCall(request).execute().body().string();
			JSONObject result = JSON.parseObject(ret);
			JSONArray array = result.getJSONArray(ZebraConstants.KEY_DATA);
			list.addAll(array.toJavaList(JSONObject.class));
			int count = list.size();
			list = list.stream().sorted(Comparator.comparing(obj -> ((JSONObject) obj).getString("date")).reversed())
					.collect(Collectors.toList());
			list = list.stream().skip((page - 1) * limit).limit(limit).collect(Collectors.toList());

			return Result.builder().withCode(result.getIntValue(ZebraConstants.KEY_CODE))
					.withMsg(result.getString("msg")).withCount(count).withData(list).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "recovery", method = { RequestMethod.GET, RequestMethod.POST })
	public Result recovery(String sid,String versionInfo) {
		try {
			if (StringUtils.isEmpty(sid)) {
				return Result.builder().withCode(ZebraConstants.FAIL).withMsg("请求参数异常！").build();
			}
			RequestBody body = new FormBody.Builder().add("sid", sid).add("versionInfo", versionInfo).build();
			Request request = new Request.Builder().url(conf.confaddr() + "/zebra-conf/recovery").post(body).build();
			String ret = client.newCall(request).execute().body().string();
			log.debug("ret={}", ret);
			JSONObject json = JSON.parseObject(ret);
			return Result.builder().withCode(json.getIntValue(ZebraConstants.KEY_CODE)).withMsg(json.getString("msg"))
					.build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}
	
	@RequestMapping(value = "listGatewayHisConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result listGatewayHisConf(int limit, int page, String sid) {
		List<JSONObject> list = Lists.newArrayList();
		try {
			RequestBody body = new FormBody.Builder().add("sid", sid).build();
			Request request = new Request.Builder().url(conf.confaddr() + "/zebra-conf/qryGatewayConfHistory").post(body)
					.build();
			String ret = client.newCall(request).execute().body().string();
			JSONObject result = JSON.parseObject(ret);
			JSONArray array = result.getJSONArray(ZebraConstants.KEY_DATA);
			list.addAll(array.toJavaList(JSONObject.class));
			int count = list.size();
			list = list.stream().sorted(Comparator.comparing(obj -> ((JSONObject) obj).getString("date")).reversed())
					.collect(Collectors.toList());
			list = list.stream().skip((page - 1) * limit).limit(limit).collect(Collectors.toList());

			return Result.builder().withCode(result.getIntValue(ZebraConstants.KEY_CODE))
					.withMsg(result.getString("msg")).withCount(count).withData(list).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}
	
	@RequestMapping(value = "gatewayConfRecovery", method = { RequestMethod.GET, RequestMethod.POST })
	public Result gatewayConfRecovery(String sid,String versionInfo) {
		try {
			if (StringUtils.isEmpty(sid)) {
				return Result.builder().withCode(ZebraConstants.FAIL).withMsg("请求参数异常！").build();
			}
			RequestBody body = new FormBody.Builder().add("sid", sid).add("versionInfo", versionInfo).build();
			Request request = new Request.Builder().url(conf.confaddr() + "/zebra-conf/gatewayConfrecovery").post(body).build();
			String ret = client.newCall(request).execute().body().string();
			log.debug("ret={}", ret);
			JSONObject json = JSON.parseObject(ret);
			return Result.builder().withCode(json.getIntValue(ZebraConstants.KEY_CODE)).withMsg(json.getString("msg"))
					.build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}
}
