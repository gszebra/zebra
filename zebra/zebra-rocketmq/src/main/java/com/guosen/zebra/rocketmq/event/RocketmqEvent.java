/**   
* @Title: RocketmqEvent.java 
* @Package com.guosen.zebra.rocketmq.event 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年1月31日 下午4:27:50 
* @version V1.0   
*/
package com.guosen.zebra.rocketmq.event;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.context.ApplicationEvent;

/**
 * @ClassName: RocketmqEvent
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2018年1月31日 下午4:27:50
 * 
 */
public class RocketmqEvent extends ApplicationEvent {
	private static final long serialVersionUID = -4468405250074063206L;
	private DefaultMQPushConsumer consumer;
	private List<MessageExt> msgs;

	public RocketmqEvent(List<MessageExt> msgs, DefaultMQPushConsumer consumer) throws Exception {
		super(msgs);
		this.consumer = consumer;
		this.setMsgs(msgs);
	}

	public String getMsg(int idx) {
		try {
			return new String(getMsgs().get(idx).getBody(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public String getMsg(int idx,String code) {
		try {
			return new String(getMsgs().get(idx).getBody(), code);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public DefaultMQPushConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(DefaultMQPushConsumer consumer) {
		this.consumer = consumer;
	}

	public MessageExt getMessageExt(int idx) {
		return getMsgs().get(idx);
	}


	public String getTopic(int idx) {
		return getMsgs().get(idx).getTopic();
	}


	public String getTag(int idx) {
		return getMsgs().get(idx).getTags();
	}


	public byte[] getBody(int idx) {
		return getMsgs().get(idx).getBody();
	}


	public String getKeys(int idx) {
		return getMsgs().get(idx).getKeys();
	}

	public List<MessageExt> getMsgs() {
		return msgs;
	}

	public void setMsgs(List<MessageExt> msgs) {
		this.msgs = msgs;
	}
}
