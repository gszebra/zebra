package com.guosen.zebra;
/**   
* @Title: ZebraRun.java 
* @Package com.guosen.zebra.core.boot.runner 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年11月13日 下午4:21:35 
* @version V1.0   
*/


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.exception.RpcBizException;
import com.guosen.zebra.core.exception.RpcErrorMsgConstant;
import com.guosen.zebra.core.exception.RpcFrameworkException;
import com.guosen.zebra.core.grpc.anotation.ZebraConf;
import com.guosen.zebra.core.grpc.util.HttpUtils;
import com.guosen.zebra.core.grpc.util.NetUtils;
import com.guosen.zebra.core.grpc.util.PropertiesContent;
import com.guosen.zebra.core.serializer.utils.JStringUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @ClassName: ZebraRun
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2017年11月13日 下午4:21:35
 * 
 */
public class ZebraRun {
	private static final Logger log = LogManager.getLogger(ZebraRun.class);
	
	public static String APP_NODE = StringUtils.EMPTY;
	public static String APP_IDC = StringUtils.EMPTY;
	public static String APP_SET = ZebraConstants.DEFAULT_SET;
	public static boolean APP_IS_WEB = false;
	public static String CONF_ADDR = "";
	public static String CONF_NAME = "";
	/** 
	* @Title: run 
	* @Description: 启动springboot
	* @param @param args main函数接收参数 主要传SET
	* @param @param clz 启动类
	* @param @param isWebEnv 是否是web应用，如果是true会启动web端口
	* @param @throws Exception    设定文件 
	* @return void    返回类型 
	* @throws 
	*/
	public static void run(String args[], Class<?> clz,boolean isWebEnv) throws Exception {
		initInParams(args);
		if(isWebEnv) APP_IS_WEB = isWebEnv;
		SpringApplication app = new SpringApplication(clz);
		ZebraConf conf = clz.getAnnotation(ZebraConf.class);
		if(!StringUtils.isEmpty(CONF_ADDR)){
			dynamicConfCenterAddr(conf);
		}
		if(conf!=null){
			CONF_NAME = conf.confName();
			Properties p = getProp(conf);
			if(p!=null){
				app.setDefaultProperties(p);
			}
		}
		if(isWebEnv){
			app.setWebApplicationType(WebApplicationType.SERVLET);
		}else{
			app.setWebApplicationType(WebApplicationType.NONE);
		}
		try{
			app.run(args);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	private static void initInParams(String args[]) throws UnsupportedEncodingException{
		try{
			if (args.length > 0) {
				String arg = args[0];
				Map<String,String> map = JStringUtils.toMap(arg);
				log.info("app started,environment is {}",map);
				if(!StringUtils.isEmpty(map.get("set"))&&!"none".equals(map.get("set").toLowerCase())){
					APP_SET = map.get("set");
				}
				if(!StringUtils.isEmpty(map.get("idc"))&&!"none".equals(map.get("idc").toLowerCase())){
					APP_IDC = map.get("idc");
				}
				if(!StringUtils.isEmpty(map.get("node"))&&!"none".equals(map.get("node").toLowerCase())){
					APP_NODE = map.get("node");
				}
				if(!StringUtils.isEmpty(map.get("host"))&&!"none".equals(map.get("host").toLowerCase())){
					NetUtils.localIp = map.get("host");
				}
				if(!StringUtils.isEmpty(map.get("conf"))&&!"none".equals(map.get("conf").toLowerCase())){
					CONF_ADDR = map.get("conf");
				}
				if(map.get("web")!=null) APP_IS_WEB = true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("resource")
	private static Properties getProp(ZebraConf conf) throws Exception{
		Properties p = new Properties();
		try {
			List<Object> list = Lists.newArrayList();
			// 资源类配置
			JSONArray array =getServiceConfig(conf.confName(), "0", conf.confaddr());
			list.addAll(array);
			// 应用类配置
			array =getServiceConfig(conf.confName(), "1", conf.confaddr());
			list.addAll(array);
			if(list.size() == 0){
				throw new RpcBizException("从配置中心获取配置异常，跳转到本地配置");
			}
			// 生成配置文件
			for (Object item : list) {
				JSONObject obj = (JSONObject)item;
				if (ZebraConstants.ZEBRA_SCOPE_GLOBAL.equals(obj.get(ZebraConstants.ZEBRA_SCOPE))
						|| (ZebraConstants.ZEBRA_SCOPE_IDC.equals(obj.get(ZebraConstants.ZEBRA_SCOPE))
								&& obj.get(ZebraConstants.ZEBRA_SCOPE_NAME).equals(APP_IDC))
						|| (ZebraConstants.ZEBRA_SCOPE_SET.equals(obj.get(ZebraConstants.ZEBRA_SCOPE))
								&& obj.get(ZebraConstants.ZEBRA_SCOPE_NAME).equals(APP_SET))
						|| (ZebraConstants.ZEBRA_SCOPE_NODE.equals(obj.get(ZebraConstants.ZEBRA_SCOPE))
								&& obj.get(ZebraConstants.ZEBRA_SCOPE_NAME).equals(APP_NODE))) {
					String txt = (String) obj.get("text");
					String args[] = txt.split("\n");
					for (String str : args) {
						if(str.startsWith("#"))continue;
						if(str.trim().length() ==0) continue;
						if(str.split("=").length==1){
							p.setProperty(str.split("=")[0], StringUtils.EMPTY);
						}else{
							p.setProperty(str.split("=")[0], str.substring(str.indexOf("=")+1));
						}
					}
				}
			}
			// 保存
			FileOutputStream out = new FileOutputStream(ZebraConstants.ZEBRA_PROPERTIES_NAME);
			// 为properties添加注释
			p.store(out, "zebra auto generate properties");
			out.close();
		} catch (Exception e) {
			log.warn("get conf from conf-center error {}" , e);
			try {
				System.err.println("**************************************");
				System.err.println("*************** 加载本地配置   *************");
				System.err.println("**************************************");
				FileInputStream in = new FileInputStream(ZebraConstants.ZEBRA_PROPERTIES_NAME);
				long size = in.getChannel().size();
				if(size==0){
					in = new FileInputStream(getLocalPath()+"/"+ZebraConstants.ZEBRA_PROPERTIES_NAME);
					size = in.getChannel().size();
					if(size==0){//去绝对路径取
						log.error(getLocalPath()+"/"+ZebraConstants.ZEBRA_PROPERTIES_NAME+" not exist");
						PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
						Properties prop = new Properties();
						Resource[] rs = resolver.getResources("classpath:*.properties");
						if(rs.length ==0){
							throw new RpcBizException(RpcErrorMsgConstant.PROPERTIES_NOT_EXISTS_EXCEPTION);
						}else{
							for (int i = 0; i < rs.length; i++) {
								Resource r = rs[i];
								prop.load(r.getInputStream());
							}
						}
					}
				}else{
					p.load(in);
					in.close();
				}
			
			} catch (IOException e1) {
				e1.printStackTrace();
				throw e1;
			}
		}
		PropertiesContent.p = p;
		return p;
	}
	
	/** 
	* @Title: getLocalPath 
	* @Description: 获取linux本地部署路径
	* @param @return    设定文件 
	* @return String    返回类型 
	* @throws 
	*/
	private static String getLocalPath() {
		String jarWholePath = ZebraRun.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		try {
			jarWholePath = java.net.URLDecoder.decode(jarWholePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.toString());
		}
		String jarPath = new File(jarWholePath).getParentFile().getAbsolutePath();
		
		return jarPath.replaceAll("\\/lib", "");
	}
	
	
	/** 
	* @Title: dynamicConfCenterAddr 
	* @Description: 通过动态配置设置配置中心地址
	* @param @param at    设定文件 
	* @return void    返回类型 
	* @throws 
	*/
	@SuppressWarnings("unchecked")
	public static void dynamicConfCenterAddr(Annotation at) {
		InvocationHandler handler = Proxy.getInvocationHandler(at);
		try {
			Field hField = handler.getClass().getDeclaredField("memberValues");
			hField.setAccessible(true);
			// 获取 memberValues
			@SuppressWarnings("rawtypes")
			Map memberValues =(Map)hField.get(handler);
			memberValues.put("confaddr", CONF_ADDR);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
	}
	
	private static JSONArray getServiceConfig(String server, String type, String addr) throws Exception {
		Map<String, String> map = Maps.newConcurrentMap();
		map.put("server", server);
		map.put("type", type);
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(HttpUtils.getUrl(addr + "/zebra-conf/getConfNew", map)).build();
		String ret = client.newCall(request).execute().body().string();
		JSONObject result = JSON.parseObject(ret);
		if (result.getIntValue(ZebraConstants.KEY_CODE) == ZebraConstants.FAIL) {
			throw new RpcFrameworkException("Request Config Center Error");
		}
		JSONArray array = result.getJSONArray(ZebraConstants.KEY_DATA);
		return array;
	}
}
