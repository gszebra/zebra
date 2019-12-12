package com.guosen.zebra.sample.controller;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * 为了样例编写和测试方便，使用Restful来做对外接口，通常对外接口都为GRPC
 */
@RestController
public class RocketMQProducerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQProducerController.class);

    private static final String TOPIC_NAME = "com-guosen-zebra-sample-rocketmq-producer";

    private static final String TAG_NAME = "tagA";
    @Autowired
    private DefaultMQProducer producer;

    @RequestMapping("/mq/produce")
    public String produce(@RequestParam("key") String key, @RequestParam("value") String value) {

        byte[] body = value.getBytes(StandardCharsets.UTF_8);
        Message message = new Message(TOPIC_NAME, TAG_NAME, key, body);

        String returnInfo = null;
        try {
            SendResult sendResult = producer.send(message);
            String messageId = sendResult.getMsgId();
            returnInfo = "Message id is : " + messageId;
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            LOGGER.error("Failed to send message", e);
            returnInfo = e.getMessage();
        }

        return returnInfo;
    }
}
