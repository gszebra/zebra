package com.guosen.zebra.cache.change.publisher;

import com.alibaba.fastjson.JSONObject;
import com.guosen.zebra.cache.change.UpdateMessage;
import com.guosen.zebra.cache.common.Constants;
import com.guosen.zebra.core.util.ServiceNameUtil;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.nio.charset.StandardCharsets;

/**
 * Rocket MQ方式缓存变更消息发布器
 */
public class CacheChangeRocketMqPublisher implements CacheChangePublisher, InitializingBean, DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheChangeRocketMqPublisher.class);

    private DefaultMQProducer producer;

    public CacheChangeRocketMqPublisher(DefaultMQProducer producer) {
        this.producer = producer;
    }

    @Override
    public void publish(UpdateMessage updateMessage) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Begin to public message {} to topic {}", updateMessage.toString(),
                    Constants.ROCKET_MQ_CACHE_CHANGE_TOPIC);
        }

        String serviceName = ServiceNameUtil.getServiceName();
        String bodyStr = JSONObject.toJSONString(updateMessage);
        Message message = new Message(Constants.ROCKET_MQ_CACHE_CHANGE_TOPIC,
                serviceName,
                bodyStr.getBytes(StandardCharsets.UTF_8));

        try {
            SendResult sendResult = producer.send(message);
            LOGGER.info("Successfully send message, message id : {}", sendResult.getMsgId());
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            // 出现了异常，我们也做不了什么了，记录下日志就好。
            LOGGER.error("Failed to send update message notification, update message : {}", updateMessage, e);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Finish to public message {} to topic {}", updateMessage.toString(),
                    Constants.ROCKET_MQ_CACHE_CHANGE_TOPIC);
        }
    }

    @Override
    public void afterPropertiesSet() throws MQClientException {
        LOGGER.info("Begin to start zebra cache sync rocket mq producer.");

        try {
            producer.start();
        }
        catch (MQClientException e) {
            LOGGER.error("Failed to start zebra cache sync rocket mq producer.", e);
            throw e;
        }

        LOGGER.info("Finish to start zebra cache sync rocket mq producer.");
    }

    @Override
    public void destroy() {
        LOGGER.info("Begin to shutdown zebra cache sync rocket mq producer.");
        producer.shutdown();
        LOGGER.info("Finish to shutdown zebra cache sync rocket mq producer.");
    }
}
