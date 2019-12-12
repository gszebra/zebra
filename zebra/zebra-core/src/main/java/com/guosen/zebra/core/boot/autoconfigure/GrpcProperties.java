package com.guosen.zebra.core.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;


/** 
* @ClassName: GrpcProperties 
* @Description: 配置读取类 
* @author 邓启翔 
* @date 2017年10月31日 上午10:36:32 
*  
*/
@ConfigurationProperties(prefix = "zebra.grpc")
public class GrpcProperties {
	private String registryAddress;
	private int port;
	private String application;
	private int maxPoolSize;
	private int corePoolSize;
	private int queueCapacity;

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public String getRegistryAddress() {
		return registryAddress;
	}

	public void setRegistryAddress(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}


	@Override
	public String toString() {
		return "GrpcProperties [registryAddress=" + registryAddress + ", port=" + port + ", application=" + application
				+ ", maxPoolSize=" + maxPoolSize + ", corePoolSize=" + corePoolSize + ", queueCapacity=" + queueCapacity
				+ "]";
	}
}
