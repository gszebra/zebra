package com.guosen.zebra.core.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;


/** 
* @ClassName: GrpcProperties 
* @Description: 配置读取类 
* @author 邓启翔 
* @date 2017年10月31日 上午10:36:32 
*  
*/
@ConfigurationProperties(prefix = "zebra.conf")
public class ZebraConfProp {
	private String addr;

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}
	
}
