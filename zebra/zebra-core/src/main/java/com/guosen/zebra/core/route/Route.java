/**   
* @Title: Route.java 
* @Package com.guosen.zebra.core.route 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年7月19日 上午11:13:39 
* @version V1.0   
*/
package com.guosen.zebra.core.route;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.coreos.jetcd.data.KeyValue;

/** 
* @ClassName: Route 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2018年7月19日 上午11:13:39 
*  
*/
public class Route {
	/** 
	* @Title: isLegalRoute 
	* @Description: 判断是否满足路由规则
	* @param @param route
	* @param @param org
	* @param @return    设定文件 
	* @return boolean    返回类型 
	* @throws 
	*/
	public static boolean isLegalRoute(String route,KeyValue org){
		if(StringUtils.isEmpty(route)) return true;
		return org.getValue().toStringUtf8().contains(route);
	}
	
	/** 
	* @Title: routeFilter 
	* @Description: 取满足规则的路由规则
	* @param @param route
	* @param @param kvs
	* @param @return    设定文件 
	* @return List<KeyValue>    返回类型 
	* @throws 
	*/
	public static List<KeyValue> routeFilter(String route,List<KeyValue> kvs){
		return kvs.stream().filter(kv -> kv.getValue().toStringUtf8().contains(route))
				.collect(Collectors.toList());
	}
}
