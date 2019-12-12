package com.guosen.zebra.sample.rocketmq.consumer.listener;

import com.guosen.zebra.rocketmq.event.RocketmqEvent;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class RocketMQListener implements ApplicationListener<RocketmqEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQListener.class);

    @Override
    public void onApplicationEvent(RocketmqEvent event) {
        List<MessageExt> msgs = event.getMsgs();
        for (MessageExt messageExt: msgs) {
            handle(messageExt);
        }
    }

    private void handle(MessageExt messageExt) {
        String key = messageExt.getKeys();
        byte[] body = messageExt.getBody();
        String value = new String(body, StandardCharsets.UTF_8);

        LOGGER.info("Received message, key : {}, value : {}", key, value);
    }
}
