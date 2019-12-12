/**   
* @Title: MainController.java 
* @Package com.yy.bg.controller 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年5月18日 下午3:13:56 
* @version V1.0   
*/
package com.guosen.zebra.conf.controller;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.guosen.zebra.conf.dto.ConfCenter;
import com.guosen.zebra.conf.dto.ConfHisCenter;
import com.guosen.zebra.conf.dto.GatwayConf;
import com.guosen.zebra.conf.dto.Result;
import com.guosen.zebra.conf.dto.SentinelDTO;
import com.guosen.zebra.conf.dto.ServVersionConf;
import com.guosen.zebra.conf.dto.ServerTest;
import com.guosen.zebra.conf.mapper.ConfMapper;
import com.guosen.zebra.core.common.ZebraConstants;

/**
 * @ClassName: UserRestController
 * @Description: 返回JSON的串的CONTROLLER
 * @author 邓启翔
 * @date 2017年5月19日 上午9:25:59
 * 
 */
@RestController
@RequestMapping("/zebra-conf")
public class ConfRestController {
	private static final Logger log = LogManager.getLogger(ConfRestController.class);

	@Autowired
	ConfMapper mapper;

	@RequestMapping("/getServerVersionConf")
	public Result getServerVersionConf(Model model) {
		try {
			Map<String, String> param = new HashMap<String, String>();
			List<Map<String, String>> list = mapper.getServerVersionConf(param);
			JSONArray array = new JSONArray();
			list.forEach(map -> {
				array.add(map);
			});
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("查询成功").withData(array).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "/updServerVersion", method = { RequestMethod.GET, RequestMethod.POST })
	public Result updServerVersion(ServVersionConf request) {
		try {
			Map<String, Object> map = Maps.newHashMap();
			map.put("SID", request.getSID());
			map.put("SERVER_NAME", request.getSERVER_NAME());
			map.put("SERVER_VERSION_DESC", request.getSERVER_VERSION_DESC());
			map.put("SERVER_VERSION", request.getSERVER_VERSION());
			int ret = 0;
			if (request.getSID() == 0) {
				ret = mapper.instServerVersion(map);
			} else {
				ret = mapper.updServerVersion(map);
			}
			log.debug("versionList={}", ret);
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("操作成功").build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("操作失败").build();
		}
	}

	@RequestMapping(value = "/getServerTest", method = { RequestMethod.GET, RequestMethod.POST })
	public Result getServerTest(ServerTest request) {
		try {
			Map<String, Object> ret = mapper.getServerTest(request);
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("查询成功").withData(ret).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "/updServerTest", method = { RequestMethod.GET, RequestMethod.POST })
	public Result updServerTest(ServerTest request) {
		try {
			request.setAttachments(URLDecoder.decode(request.getAttachments(), "utf-8"));
			request.setRequest(URLDecoder.decode(request.getRequest(), "utf-8"));
			request.setResponse(URLDecoder.decode(request.getResponse(), "utf-8"));
			int ret = mapper.updServerTest(request);
			if (ret == 0) {
				ret = mapper.insertServerTest(request);
			}
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("操作成功").build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("操作失败").build();
		}
	}

	@RequestMapping(value = "/getGatewayConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result getGatewayConf() {
		try {
			List<GatwayConf> ret = mapper.getGatewayConf();
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("查询成功").withData(ret).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "/updGatewayConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result updGatewayConf(@RequestBody GatwayConf request, HttpServletRequest http) {
		try {
			log.debug("updGatewayConf param = {},http={}", request, http.getParameterMap());
			if (StringUtils.isEmpty(request.getGroup())) {
				request.setGroup(ZebraConstants.DEFAULT_GROUP);
			}
			if (StringUtils.isEmpty(request.getVersion())) {
				request.setVersion(ZebraConstants.DEFAULT_VERSION);
			}
			if (StringUtils.isEmpty(request.getSet())) {
				request.setSet(ZebraConstants.DEFAULT_SET);
			}
			request.setTag(URLDecoder.decode(request.getTag(), "utf-8"));
			request.setText(URLDecoder.decode(request.getText(), "utf-8"));
			long timestamp = System.currentTimeMillis();
			request.setVersionInfo(timestamp+"");
			mapper.bakGatewayConf(request);
			mapper.updGatewayConf(request);
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("操作成功").build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("操作失败").build();
		}
	}

	@RequestMapping(value = "/delGatewayConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result delGatewayConf(@RequestBody GatwayConf request) {
		try {
			long timestamp = System.currentTimeMillis();
			request.setVersionInfo(timestamp+"");
			mapper.bakGatewayConf(request);
			mapper.delGatewayConf(request);
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("操作成功").build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("操作失败").build();
		}
	}

	@RequestMapping(value = "/insertGatewayConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result insertGatewayConf(@RequestBody GatwayConf request) {
		try {
			if (StringUtils.isEmpty(request.getGroup())) {
				request.setGroup(ZebraConstants.DEFAULT_GROUP);
			}
			if (StringUtils.isEmpty(request.getVersion())) {
				request.setVersion(ZebraConstants.DEFAULT_VERSION);
			}
			if (StringUtils.isEmpty(request.getSet())) {
				request.setSet(ZebraConstants.DEFAULT_SET);
			}
			request.setTag(URLDecoder.decode(request.getTag(), "utf-8"));
			request.setText(URLDecoder.decode(request.getText(), "utf-8"));
			mapper.insertGatewayConf(request);
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("操作成功").build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("操作失败").build();
		}
	}

	@RequestMapping(value = "/getConf", method = { RequestMethod.GET, RequestMethod.POST })
	public List<ConfCenter> getConf(ConfCenter param) {
		try {
			if (StringUtils.isEmpty(param.getServer())) {
				List<ConfCenter> ret = mapper.getAllConf(param);
				return ret;
			}
			List<ConfCenter> ret = mapper.getConf(param);
			return ret;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	@RequestMapping(value = "/getConfNew", method = { RequestMethod.GET, RequestMethod.POST })
	public Result getConfNew(ConfCenter param) {
		try {
			if (StringUtils.isEmpty(param.getServer())) {
				List<ConfCenter> ret = mapper.getAllConf(param);
				return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("查询成功").withData(ret).build();
			}
			List<ConfCenter> ret = mapper.getConf(param);
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("查询成功").withData(ret).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "/updConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result updConf(@RequestBody ConfCenter request) {
		try {
			request.setText(URLDecoder.decode(request.getText(), "utf-8"));
			int ret = mapper.updConf(request);
			if (ret == 0) {
				mapper.insertConf(request);
			}
			ConfCenter cc = mapper.getConfById(request);
			long timestamp = System.currentTimeMillis();
			cc.setVersionInfo(timestamp+"");
			mapper.insertConfHistory(cc);
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("操作成功").build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("操作失败").build();
		}
	}

	@RequestMapping(value = "/delConf", method = { RequestMethod.GET, RequestMethod.POST })
	public Result delConf(ConfCenter request) {
		try {
			ConfCenter cc = mapper.getConfById(request);
			long timestamp = System.currentTimeMillis();
			cc.setVersionInfo(timestamp+"");
			mapper.insertConfHistory(cc);
			mapper.delConf(request);
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("操作成功").build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("操作失败").build();
		}
	}

	@RequestMapping(value = "/qryConfHistory", method = { RequestMethod.GET, RequestMethod.POST })
	public Result qryConfHistory(ConfCenter request) {
		try {
			List<ConfHisCenter> ret = mapper.getHistoryConf(request);
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("查询成功").withData(ret).build();
		} catch (Exception e) {
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}

	@RequestMapping(value = "/recovery", method = { RequestMethod.GET, RequestMethod.POST })
	public Result recovery(ConfCenter request) {
		try {
			mapper.centerRecovery(request);
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("恢复成功").build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("恢复失败").build();
		}
	}

	@RequestMapping(value = "/saveSentinel", method = { RequestMethod.GET, RequestMethod.POST })
	public Result saveSentinel(SentinelDTO request) {
		Result.Builder ret = Result.builder();
		try {
			log.debug("begin save sentine param ={}", request);
			if (request.getId() == 0) {
				mapper.createSentinel(request);
			} else {
				mapper.updSentinel(request);
			}
			ret.withCode(ZebraConstants.SUCCESS);
			ret.withMsg("调用成功");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			ret.withCode(ZebraConstants.FAIL);
			ret.withMsg("系统异常");
		}
		return ret.build();
	}
	
	@RequestMapping(value = "/delSentinel", method = { RequestMethod.GET, RequestMethod.POST })
	public Result delSentinel(SentinelDTO request) {
		Result.Builder ret = Result.builder();
		try {
			log.debug("begin del sentine param ={}", request);
			mapper.delSentinel(request);
			ret.withCode(ZebraConstants.SUCCESS);
			ret.withMsg("调用成功");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			ret.withCode(ZebraConstants.FAIL);
			ret.withMsg("系统异常");
		}
		return ret.build();
	}

	@RequestMapping(value = "/qrySentinel", method = { RequestMethod.GET, RequestMethod.POST })
	public Result qrySentinel(SentinelDTO request) {
		Result.Builder ret = Result.builder();
		try {
			log.debug("begin query sentine param ={}", request);
			List<SentinelDTO> list = mapper.qrySentinel(request);
			ret.withCode(ZebraConstants.SUCCESS);
			ret.withMsg("调用成功");
			ret.withData(list);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			ret.withCode(ZebraConstants.FAIL);
			ret.withMsg("系统异常");
		}
		return ret.build();
	}
	
	@RequestMapping(value = "/qryConfStatics", method = { RequestMethod.GET, RequestMethod.POST })
	public Result qryConfStatics() {
		Result.Builder ret = Result.builder();
		try {
			int confCount = mapper.qryConfStatics();
			int testCount = mapper.qryTestConfStatics();
			JSONObject json = new JSONObject();
			json.put("confCount", confCount);
			json.put("testCount", testCount);
			ret.withCode(ZebraConstants.SUCCESS);
			ret.withMsg("调用成功");
			ret.withData(json);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			ret.withCode(ZebraConstants.FAIL);
			ret.withMsg("系统异常");
		}
		return ret.build();
	}
	
	@RequestMapping(value = "/qryGatewayConfHistory", method = { RequestMethod.GET, RequestMethod.POST })
	public Result qryGatewayConfHistory(ConfCenter request) {
		try {
			List<GatwayConf> ret = mapper.getGatewayHistConf(request);
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("查询成功").withData(ret).build();
		} catch (Exception e) {
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("系统异常").build();
		}
	}
	
	@RequestMapping(value = "/gatewayConfrecovery", method = { RequestMethod.GET, RequestMethod.POST })
	public Result gatewayConfrecovery(ConfCenter request) {
		try {
			mapper.gatewayConfRecovery(request);
			return Result.builder().withCode(ZebraConstants.SUCCESS).withMsg("恢复成功").build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.builder().withCode(ZebraConstants.FAIL).withMsg("恢复失败").build();
		}
	}
}