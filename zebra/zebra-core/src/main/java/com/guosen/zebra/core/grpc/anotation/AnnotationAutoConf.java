/**   
* @Title: AnotationAutoConf.java 
* @Package com.guosen.zebra.core.grpc.anotation 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年7月17日 下午2:42:34 
* @version V1.0   
*/
package com.guosen.zebra.core.grpc.anotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import com.guosen.zebra.core.grpc.util.PropertiesContent;
import com.guosen.zebra.core.grpc.util.RegExp;

/** 
* @ClassName: AnotationAutoConf 
* @Description: 动态初始化注解的属性值
* @author 邓启翔 
* @date 2018年7月17日 下午2:42:34 
*  
*/
public class AnnotationAutoConf {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized static void autoConf(Annotation at) {
		InvocationHandler handler = Proxy.getInvocationHandler(at);
		try {
			Field hField = handler.getClass().getDeclaredField("memberValues");
			hField.setAccessible(true);
			// 获取 memberValues
			Map memberValues = (Map) hField.get(handler);
			memberValues.forEach((k, v) -> {
				if (v instanceof String) {
					List<String> confList = getKeywords((String) v);
					if (confList.size() > 0) {
						String[] confs = confList.get(0).split(":");
						if (confs.length > 0) {
							String defualt = (confs.length == 2 ? confs[1] : null);
							try {
								memberValues.put(k, PropertiesContent.getStrValue(confs[0], defualt));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}

	}
	
	public static List<String> getKeywords(String p){
        String reg = "(?<=(?<!\\\\)\\$\\{)(.*?)(?=(?<!\\\\)\\})";    
        RegExp re = new RegExp();
        List<String> list = re.find(reg, p);
        return list;
    }
}
