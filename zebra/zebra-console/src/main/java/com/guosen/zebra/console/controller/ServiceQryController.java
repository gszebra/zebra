package com.guosen.zebra.console.controller;

import java.net.URLEncoder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guosen.App;
import com.guosen.zebra.console.dto.MonitorDTO;
import com.guosen.zebra.console.dto.RegitryBaseInfo;
import com.guosen.zebra.console.dto.Result;
import com.guosen.zebra.console.dto.ServiceDetail;
import com.guosen.zebra.console.service.ActiveProbingService;
import com.guosen.zebra.console.service.ConsoleSerivce;
import com.guosen.zebra.console.utils.ExceptionUtil;
import com.guosen.zebra.core.boot.autoconfigure.ZebraConfProp;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.anotation.ZebraConf;
import com.guosen.zebra.core.grpc.anotation.ZebraReference;
import com.guosen.zebra.core.grpc.server.GenericService;
import com.guosen.zebra.core.grpc.util.HttpUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;

@RestController
@RequestMapping(value = "/api/service")
@EnableConfigurationProperties(ZebraConfProp.class)
public class ServiceQryController {
	private static final Logger log = LogManager.getLogger(ServiceQryController.class);

	@ZebraReference(timeOut = 600000)
	private GenericService genericService;

	@Autowired
	private ConsoleSerivce consoleSerivce;

	@Value("${zebra.console.userCode:}")
	private String testUserCode;

	ZebraConf conf = App.class.getAnnotation(ZebraConf.class);

	private final OkHttpClient client = new OkHttpClient();

	@Autowired
	private ActiveProbingService activePorbing;

	@RequestMapping(value = "versionList", method = { RequestMethod.GET, RequestMethod.POST })
	public Result versionList() {
		List<JSONObject> list = Lists.newArrayList();
		try {
			Request request = new Request.Builder().url(conf.confaddr() + "/zebra-conf/getServerVersionConf").build();
			String ret = client.newCall(request).execute().body().string();
			JSONObject result = JSON.parseObject(ret);
			if (result.getInteger(ZebraConstants.KEY_CODE) == ZebraConstants.FAIL) {
				return Result.builder().withCode(result.getIntValue(ZebraConstants.KEY_CODE))
						.withMsg(result.getString("msg")).build();
			}
			JSONArray array = result.getJSONArray(ZebraConstants.KEY_DATA);
			list.addAll(array.toJavaList(JSONObject.class));
			return Result.builder().withCode(result.getIntValue(ZebraConstants.KEY_CODE))
					.withMsg(result.getString("msg")).withData(list).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "updServerVersion", method = { RequestMethod.GET, RequestMethod.POST })
	public Result updServerVersion(int SID, String SERVER_NAME, String SERVER_VERSION_DESC, String SERVER_VERSION) {
		try {
			Map<String, String> map = Maps.newConcurrentMap();
			map.put("SID", SID + "");
			map.put("SERVER_NAME", SERVER_NAME);
			map.put("SERVER_VERSION_DESC", URLEncoder.encode(SERVER_VERSION_DESC, "utf-8"));
			map.put("SERVER_VERSION", SERVER_VERSION);
			Request request = new Request.Builder()
					.url(HttpUtils.getUrl(conf.confaddr() + "/zebra-conf/updServerVersion", map)).build();
			String ret = client.newCall(request).execute().body().string();
			JSONObject result = JSON.parseObject(ret);
			log.debug("ret={}", result);
			return Result.builder().withCode(result.getIntValue(ZebraConstants.KEY_CODE))
					.withMsg(result.getString("msg")).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "insertServerVersion", method = { RequestMethod.GET, RequestMethod.POST })
	public Result insertServerVersion(String SERVER_NAME, String SERVER_VERSION_DESC, String SERVER_VERSION) {
		try {
			Map<String, String> map = Maps.newHashMap();
			map.put("SERVER_NAME", SERVER_NAME);
			map.put("SERVER_VERSION_DESC", URLEncoder.encode(SERVER_VERSION_DESC, "utf-8"));
			map.put("SERVER_VERSION", SERVER_VERSION);
			Request request = new Request.Builder()
					.url(HttpUtils.getUrl(conf.confaddr() + "/zebra-conf/updServerVersion", map)).build();
			String ret = client.newCall(request).execute().body().string();
			JSONObject result = JSON.parseObject(ret);
			log.debug("ret={}", result);
			return Result.builder().withCode(result.getIntValue(ZebraConstants.KEY_CODE))
					.withMsg(result.getString("msg")).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "searchServiceTest", method = { RequestMethod.GET, RequestMethod.POST })
	public Result searchServiceTest(String server, String method) {
		try {
			Map<String, String> map = Maps.newHashMap();
			map.put("server", server);
			map.put("method", method);
			Request request = new Request.Builder()
					.url(HttpUtils.getUrl(conf.confaddr() + "/zebra-conf/getServerTest", map)).build();
			String ret = client.newCall(request).execute().body().string();
			JSONObject result = JSON.parseObject(ret);
			log.debug("ret={}", result);
			return Result.builder().withCode(result.getIntValue(ZebraConstants.KEY_CODE))
					.withMsg(result.getString("msg")).withData(result.get("data")).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "serviceTest", method = { RequestMethod.GET, RequestMethod.POST })
	public JSONObject serviceTest(String server, String method, String group, String set, String version,
			String request, String response, String attachments, String descript) {
		try {
			log.info("request={}", request);
			Map<String, String> map = Maps.newHashMap();
			map.put("server", server);
			map.put("method", method);
			map.put("request", URLEncoder.encode(request, "utf-8"));
			map.put("response", URLEncoder.encode(response, "utf-8"));
			map.put("attachments", URLEncoder.encode(attachments, "utf-8"));
			map.put("descript", URLEncoder.encode(descript, "utf-8"));
			if (StringUtils.isEmpty(version)) {
				version = ZebraConstants.DEFAULT_VERSION;
			}
			if (StringUtils.isEmpty(group)) {
				group = ZebraConstants.DEFAULT_GROUP;
			}
			if (StringUtils.isEmpty(set)) {
				set = ZebraConstants.DEFAULT_SET;
			}

			Request httpRequest = new Request.Builder()
					.url(HttpUtils.getUrl(conf.confaddr() + "/zebra-conf/updServerTest", map)).build();
			client.newCall(httpRequest).execute();

			if (response == null)
				response = StringUtils.EMPTY;
			JSONObject obj = JSON.parseObject(request);
			if (obj == null) {
				obj = new JSONObject();
			}
			if (!StringUtils.isEmpty(attachments)) {
				RpcContext.getContext().getAttachments().putAll(JSON.parseObject(attachments, Map.class));
			} else {
				Map<String, String> attachMap = initAttatch(null, testUserCode);
				RpcContext.getContext().setAttachments(attachMap);
			}
			obj = (JSONObject) genericService.$invoke(server, group, version, set, method, obj);
			boolean isMatchResult = true;
			if (!StringUtils.isEmpty(response)) {
				JSONObject target = JSON.parseObject(response);
				Set<String> keys = target.keySet();
				for (String key : keys) {
					if (!target.get(key).equals(obj.get(key))) {
						isMatchResult = false;
						break;
					}
				}
			} else {
				isMatchResult = false;
			}

			JSONObject ret = new JSONObject();
			ret.put("isMatchResult", isMatchResult);
			ret.put("ret", obj);
			ret.put("code", 0);
			log.debug(ret);
			return ret;
		} catch (Exception e) {
			JSONObject ret = new JSONObject();
			ret.put("isMatchResult", false);
			ret.put("ret", ExceptionUtil.printStack(e));
			ret.put("code", 1);
			log.error(e.getMessage(), e);
			return ret;
		}
	}

	@RequestMapping(value = "methodTest", method = { RequestMethod.GET, RequestMethod.POST })
	public JSONObject methodTest(String service, String method, String set) {
		String retStr = null;
		try {
			Map<String, String> map = Maps.newHashMap();
			map.put("server", service);
			map.put("method", method);
			Request request = new Request.Builder()
					.url(HttpUtils.getUrl(conf.confaddr() + "/zebra-conf/getServerTest", map)).build();
			retStr = client.newCall(request).execute().body().string();

			if (StringUtils.isEmpty(retStr)) {
				JSONObject ret = new JSONObject();
				ret.put("code", "-1");
				ret.put("msg", "请求参数未配置");
				return ret;
			}
			JSONObject obj = JSON.parseObject(retStr);
			JSONObject req = JSON.parseObject(obj.getString("REQUEST"));

			String attach = obj.getString("ATTACHMENTS");
			if (req == null) {
				JSONObject ret = new JSONObject();
				ret.put("code", "-1");
				ret.put("msg", "请求参数未配置");
				return ret;
			}
			Map<String, String> attachMap = initAttatch(attach, req.getString(ZebraConstants.USER_TAG));
			RpcContext.getContext().setAttachments(attachMap);
			if (StringUtils.isEmpty(set)) {
				set = ZebraConstants.DEFAULT_SET;
			}
			JSONObject reply = (JSONObject) genericService.$invoke(service, ZebraConstants.DEFAULT_GROUP,
					ZebraConstants.DEFAULT_VERSION, set, method, req);
			boolean isMatchResult = true;

			if (!StringUtils.isEmpty(obj.getString("RESPONSE"))) {
				JSONObject target = JSON.parseObject(obj.getString("RESPONSE"));
				Set<String> keys = target.keySet();
				for (String key : keys) {
					if (target.get(key) == null)
						continue;
					if (!target.get(key).equals(reply.get(key))) {
						isMatchResult = false;
						break;
					}
				}
			} else {
				isMatchResult = false;
			}

			JSONObject ret = new JSONObject();
			if (isMatchResult) {
				ret.put("code", "1");
				ret.put("msg", "服务正常");
			} else if (reply.containsKey("result")) {
				try {
					JSONArray array = reply.getJSONArray("result");
					JSONObject json = array.getJSONObject(0);
					if ("0".equals(json.get("code"))) {
						ret.put("code", "1");
						ret.put("msg", "服务正常");
					} else {
						ret.put("code", "2");
						ret.put("msg", "请求正常返回，但返回参数匹配与预定义的匹配不上");
					}
				} catch (Exception e) {
					ret.put("code", "2");
					ret.put("msg", "请求正常返回，但返回参数匹配与预定义的匹配不上");
					return ret;
				}
			} else {
				ret.put("code", "2");
				ret.put("msg", "请求正常返回，但返回参数匹配与预定义的匹配不上");
			}
			ret.put("isMatchResult", isMatchResult);
			ret.put("ret", reply);
			log.debug(ret);
			return ret;
		} catch (Exception e) {
			JSONObject ret = new JSONObject();
			ret.put("code", "0");
			ret.put("msg", "服务异常");
			ret.put("isMatchResult", false);
			ret.put("ret", e.getMessage());
			log.info("error param service ={},method={},retStr ={}", service, method, retStr);
			log.error(e.getMessage(), e);
			return ret;
		}
	}

	@RequestMapping(value = "zabixSurvey", method = { RequestMethod.GET, RequestMethod.POST })
	public Result zabixSurvey() {
		try {
			Map<String, MonitorDTO> map = activePorbing.getMonitorResult();
			return Result.builder().withCode(0).withMsg("查询完成").withData(map.values()).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(1).withMsg("系统异常：" + e.getMessage()).build();
		}
	}

	@RequestMapping(value = "monitorList", method = { RequestMethod.GET, RequestMethod.POST })
	public Result monitorList(int limit, int page, String searchTxt) {
		List<JSONObject> ret = consoleSerivce.monitorList();
		if (ret.size() == 0) {
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("监控中心异常，请稍后再试！").build();
		}
		int count = ret.size();
		if (!StringUtils.isEmpty(searchTxt)) {
			ret = ret.stream()
					.filter(obj -> obj.getString("gRpcService").contains(searchTxt)
							|| obj.getString("instance").contains(searchTxt)
							|| obj.getString("gRpcMethod").contains(searchTxt))
					.collect(Collectors.toList());
		}
		ret = ret.stream().sorted(Comparator.comparing(obj -> ((JSONObject) obj).getString("gRpcService"))
				.thenComparing(obj -> ((JSONObject) obj).getString("gRpcMethod"))).collect(Collectors.toList());
		ret = ret.stream().skip((page - 1) * limit).limit(limit).collect(Collectors.toList());
		return Result.builder().withCode(ZebraConstants.SUCCESS).withData(ret).withCount(count).build();
	}

	@RequestMapping(value = "etcdList", method = { RequestMethod.GET, RequestMethod.POST })
	public Result etcdList(int limit, int page, String searchTxt) {
		List<RegitryBaseInfo> services = consoleSerivce.getZebraService();
		if (services.size() == 0) {
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("注册中心无服务！").build();
		}
		int count = services.size();
		if (!StringUtils.isEmpty(searchTxt)) {
			services = services.stream().filter(obj -> obj.getService().contains(searchTxt))
					.collect(Collectors.toList());
		}
		services = services.stream().sorted(Comparator.comparing(RegitryBaseInfo::getService).reversed())
				.collect(Collectors.toList());
		services = services.stream().skip((page - 1) * limit).limit(limit).collect(Collectors.toList());
		return Result.builder().withCode(ZebraConstants.SUCCESS).withData(services).withCount(count).build();
	}

	@RequestMapping(value = "searchMethod", method = { RequestMethod.GET, RequestMethod.POST })
	public Result searchMethod(int limit, int page, String searchTxt) {
		try {
			if (StringUtils.isEmpty(searchTxt)) {
				return Result.builder().withCode(ZebraConstants.FAIL).withMsg("请输入查询服务名称！").build();
			}
			List<ServiceDetail> list = consoleSerivce.getMethod(searchTxt);
			int count = list.size();
			list = list.stream().sorted(Comparator.comparing(ServiceDetail::getMethod)).collect(Collectors.toList());
			list = list.stream().skip((page - 1) * limit).limit(limit).collect(Collectors.toList());
			return Result.builder().withCode(ZebraConstants.SUCCESS).withData(list).withCount(count).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private Map<String, String> initAttatch(String attach, String userCode) {
		Map<String, String> attachMap = Maps.newHashMap();
		if (!StringUtils.isEmpty(attach)) {
			JSON.parseObject(attach, new TypeReference<Map<String, String>>() {
			});
		}
		return attachMap;
	}

}