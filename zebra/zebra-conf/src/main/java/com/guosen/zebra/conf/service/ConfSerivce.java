/**   
* @Title: RegistrySerivce.java 
* @Package com.guosen.zebra.console.service 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年11月22日 下午1:29:53 
* @version V1.0   
*/
package com.guosen.zebra.conf.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;

/** 
* @ClassName: RegistrySerivce 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2017年11月22日 下午1:29:53 
*/
@Service
public class ConfSerivce  implements ApplicationListener<WebServerInitializedEvent> {
	@Autowired
	private EtcdRegistry etcdRegistry;
	@Override
	public void onApplicationEvent(WebServerInitializedEvent event) {
		try {
			int port = event.getWebServer().getPort();
			etcdRegistry.register("conf",ZebraConstants.TYPE_CONF,port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
