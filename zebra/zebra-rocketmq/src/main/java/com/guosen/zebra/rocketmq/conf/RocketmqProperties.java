/**   
* @Title: RocketmqProperties.java 
* @Package com.guosen.zebra.rocketmq.conf 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年1月31日 下午4:19:33 
* @version V1.0   
*/
package com.guosen.zebra.rocketmq.conf;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @ClassName: RocketmqProperties
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2018年1月31日 下午4:19:33
 * 
 */
@ConfigurationProperties(RocketmqProperties.PREFIX)
public class RocketmqProperties {
	public static final String PREFIX = "zebra.rocketmq";
	private String namesrvAddr;
	private String producerGroupName;  
	private String transactionProducerGroupName;  
	private String consumerGroupName;  
	private String producerInstanceName;
	private String consumerInstanceName;
	private String producerTranInstanceName;
	private int consumerBatchMaxSize;
	private boolean consumerBroadcasting;
	private boolean enableHisConsumer;
	private boolean enableOrderConsumer;
	private List<String> subscribe = new ArrayList<>();
	
	public String getNamesrvAddr() {
		return namesrvAddr;
	}
	public void setNamesrvAddr(String namesrvAddr) {
		this.namesrvAddr = namesrvAddr;
	}
	public String getProducerGroupName() {
		return producerGroupName;
	}
	public void setProducerGroupName(String producerGroupName) {
		this.producerGroupName = producerGroupName;
	}
	public String getConsumerGroupName() {
		return consumerGroupName;
	}
	public void setConsumerGroupName(String consumerGroupName) {
		this.consumerGroupName = consumerGroupName;
	}
	public String getProducerInstanceName() {
		return producerInstanceName;
	}
	public void setProducerInstanceName(String producerInstanceName) {
		this.producerInstanceName = producerInstanceName;
	}
	public String getConsumerInstanceName() {
		return consumerInstanceName;
	}
	public void setConsumerInstanceName(String consumerInstanceName) {
		this.consumerInstanceName = consumerInstanceName;
	}
	public String getTransactionProducerGroupName() {
		return transactionProducerGroupName;
	}
	public void setTransactionProducerGroupName(String transactionProducerGroupName) {
		this.transactionProducerGroupName = transactionProducerGroupName;
	}
	public String getProducerTranInstanceName() {
		return producerTranInstanceName;
	}
	public void setProducerTranInstanceName(String producerTranInstanceName) {
		this.producerTranInstanceName = producerTranInstanceName;
	}
	public List<String> getSubscribe() {
		return subscribe;
	}
	public void setSubscribe(List<String> subscribe) {
		this.subscribe = subscribe;
	}
	public int getConsumerBatchMaxSize() {
		return consumerBatchMaxSize;
	}
	public void setConsumerBatchMaxSize(int consumerBatchMaxSize) {
		this.consumerBatchMaxSize = consumerBatchMaxSize;
	}
	public boolean isConsumerBroadcasting() {
		return consumerBroadcasting;
	}
	public void setConsumerBroadcasting(boolean consumerBroadcasting) {
		this.consumerBroadcasting = consumerBroadcasting;
	}
	public boolean isEnableHisConsumer() {
		return enableHisConsumer;
	}
	public void setEnableHisConsumer(boolean enableHisConsumer) {
		this.enableHisConsumer = enableHisConsumer;
	}
	public boolean isEnableOrderConsumer() {
		return enableOrderConsumer;
	}
	public void setEnableOrderConsumer(boolean enableOrderConsumer) {
		this.enableOrderConsumer = enableOrderConsumer;
	}
}
