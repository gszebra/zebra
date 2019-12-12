package com.guosen.zebra.console.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guosen.App;
import com.guosen.zebra.console.dto.Result;
import com.guosen.zebra.console.dto.ServiceInfo;
import com.guosen.zebra.console.service.ConsoleSerivce;
import com.guosen.zebra.core.grpc.anotation.ZebraConf;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;

@RestController
@RequestMapping({ "/api/user" })
public class HomeController {
	private static final Logger log = LogManager.getLogger(HomeController.class);
	@Value("${zebra.console.username:admin}")
	private String username;
	@Value("${zebra.console.password:admin}")
	private String password;
	@Autowired
	private ConsoleSerivce consoleService;
	private ZebraConf conf = (ZebraConf) App.class.getAnnotation(ZebraConf.class);
	private final OkHttpClient client = new OkHttpClient();

	@RequestMapping(value = { "/login" }, method = { RequestMethod.GET, RequestMethod.POST })
	public Result login(String username, String password, HttpServletRequest req) {
		try {
			if (this.username.equals(username) && this.password.equals(password)) {
				req.getSession(true).setAttribute("name", username);
				return Result.builder().withCode(0).withMsg("登入成功").build();
			} else {
				return Result.builder().withCode(1).withMsg("用户名密码错误").build();
			}
		} catch (Exception var5) {
			log.error(var5.getMessage(), var5);
			return Result.builder().withCode(1).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = { "/qryStatics" }, method = { RequestMethod.GET, RequestMethod.POST })
	public Result qryStatics() {
		try {
			JSONObject json = new JSONObject();
			List<ServiceInfo> services = this.consoleService.getAllApplication();
			json.put("sCount", services.size());
			int ips = 0;
			for (ServiceInfo info : services) {
				ips = ips + info.getAddrs().size();
			}

			json.put("dCount", ips);
			Request request = (new Builder()).url(this.conf.confaddr() + "/zebra-conf/qryConfStatics").build();
			String ret = this.client.newCall(request).execute().body().string();
			log.debug("versionList={}", ret);
			JSONObject result = JSON.parseObject(ret);
			json.put("confCount", result.getJSONObject("data").get("confCount"));
			json.put("testCount", result.getJSONObject("data").get("testCount"));
			List<JSONObject> list = this.consoleService.monitorList();
			int i1 = 0;
			int i2 = 0;
			int i3 = 0;
			int i4 = 0;
			int i5 = 0;
			for (JSONObject j : list) {
				int latency = j.getIntValue("avgLatency");
				if (0 <= latency && latency < 100) {
					++i1;
				} else if (100 <= latency && latency < 500) {
					++i2;
				} else if (500 <= latency && latency < 1000) {
					++i3;
				} else if (1000 <= latency && latency <= 3000) {
					++i4;
				} else {
					++i5;
				}
			}
			json.put("i1", i1);
			json.put("i2", i2);
			json.put("i3", i3);
			json.put("i4", i4);
			json.put("i5", i5);
			return Result.builder().withCode(0).withData(json).build();
		} catch (Exception var16) {
			log.error(var16.getMessage(), var16);
			return Result.builder().withCode(1).withMsg("系统异常").build();
		}
	}
}