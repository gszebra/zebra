package com.guosen.zebra.core.boot.controller;

import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.grpc.anotation.ZebraReference;
import com.guosen.zebra.core.grpc.anotation.ZebraService;
import com.guosen.zebra.core.grpc.server.GenericService;
import com.guosen.zebra.core.util.ApplicationContextUtil;

@RestController
@RequestMapping("service")
public class ZebraTestController {
	private static final Logger log = LogManager.getLogger(ZebraTestController.class);

	@ZebraReference(timeOut = 3000000, route = "localhost")
	private GenericService genericService;

	@RequestMapping(value = "test", method = RequestMethod.GET)
	public String testMethod(String method, String param, String attach) throws Exception {
		try {
			JSONObject json = new JSONObject(getParamMap(param));
			Collection<Object> list = ApplicationContextUtil.getTypedBeansWithAnnotation(ZebraService.class);
			Object service = list.stream().findFirst().get();
			ZebraService zebra = service.getClass().getAnnotation(ZebraService.class);
			String serviceName = zebra.service();
			if (StringUtils.isEmpty(serviceName)) {
				Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(service.getClass());
				for (Class<?> interfaceClass : interfaces) {
					String interfaceName = interfaceClass.getName();
					if (!StringUtils.startsWith(interfaceName, "org.springframework")
							&& !StringUtils.startsWith(interfaceName, "java.")) {
						serviceName = interfaceName;
					}
				}
			}
			if (!StringUtils.isEmpty(attach)) {
				RpcContext.getContext().getAttachments().putAll(getAttachMap(attach));
			}
			JSONObject reply = genericService.$invoke(serviceName, zebra.group(), zebra.version(), zebra.set(), method,
					json);
			return reply.toJSONString();
		} catch (Exception e) {
			log.error("local test error method ={},param={},attach ={}", method, param, attach);
			log.error("local test error " + e.getMessage(), e);
			throw e;
		}
	}

	@RequestMapping(value = "postTest", method = RequestMethod.POST)
	public String postTestMethod(@RequestBody JSONObject json) throws Exception {
		try {
			Collection<Object> list = ApplicationContextUtil.getTypedBeansWithAnnotation(ZebraService.class);
			Object service = list.stream().findFirst().get();
			ZebraService zebra = service.getClass().getAnnotation(ZebraService.class);
			String serviceName = zebra.service();
			if (StringUtils.isEmpty(serviceName)) {
				Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(service.getClass());
				for (Class<?> interfaceClass : interfaces) {
					String interfaceName = interfaceClass.getName();
					if (!StringUtils.startsWith(interfaceName, "org.springframework")
							&& !StringUtils.startsWith(interfaceName, "java.")) {
						serviceName = interfaceName;
					}
				}
			}
			Map<String, String> attach = json.getObject("attach", new TypeReference<Map<String, String>>() {
			});
			if (attach != null) {
				RpcContext.getContext().getAttachments().putAll(attach);
			}
			String method = json.getString("method");
			JSONObject param = json.getJSONObject("param");
			JSONObject reply = genericService.$invoke(serviceName, zebra.group(), zebra.version(), zebra.set(), method,
					param);
			return reply.toJSONString();
		} catch (Exception e) {
			log.error("local test error json={}", json);
			log.error("local test error " + e.getMessage(), e);
			throw e;
		}
	}

	@RequestMapping(value = "check", method = RequestMethod.GET)
	public String check() throws Exception {
		try {
			Collection<Object> list = ApplicationContextUtil.getTypedBeansWithAnnotation(ZebraService.class);
			if (list.size() > 0) {
				return "check success";
			} else {
				return "check error";
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private Map<String, Object> getParamMap(String data) throws Exception {
		Map<String, Object> paramMap = new HashMap<>();
		data = URLDecoder.decode(data, "utf-8");// 进行一次urldecode
		String arrays[] = data.split(",");
		for (String item : arrays) {
			if (item.split("=").length > 1) {
				paramMap.put(item.split("=")[0], item.split("=")[1]);
			} else {
				paramMap.put(item.split("=")[0], "");
			}
		}
		return paramMap;
	}

	private Map<String, String> getAttachMap(String data) throws Exception {
		Map<String, String> paramMap = new HashMap<>();
		data = URLDecoder.decode(data, "utf-8");// 进行一次urldecode
		String arrays[] = data.split(",");
		for (String item : arrays) {
			if (item.split("=").length > 1) {
				paramMap.put(item.split("=")[0], item.split("=")[1]);
			} else {
				paramMap.put(item.split("=")[0], "");
			}
		}
		return paramMap;
	}

	public static void main(String args[]) {
		String jsonStr = "{\"method\":\"getRecommendedFlow\",\"param\":{\"custid\":\"110002345509\",\"ip\":\"localhost\"},\"attach\":{\"softName\":\"goldsun_android\",\"sysVer\":\"4.8.0\"}}";
		JSONObject json = JSON.parseObject(jsonStr);
		Map<String, String> attach = json.getObject("attach", new TypeReference<Map<String, String>>() {
		});
		System.err.println(attach);
	}
}
