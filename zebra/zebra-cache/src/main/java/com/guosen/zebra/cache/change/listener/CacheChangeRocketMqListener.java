package com.guosen.zebra.cache.change.listener;

import com.guosen.zebra.cache.change.handler.rocketmq.RocketMqMessageHandler;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * RocketMQ方式的缓存变更通知监听器
 */
public class CacheChangeRocketMqListener implements MessageListenerConcurrently {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheChangeRocketMqListener.class);
    private RocketMqMessageHandler handler;

    public CacheChangeRocketMqListener(RocketMqMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        ConsumeConcurrentlyStatus status = ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

        for (MessageExt messageExt : msgs) {
            try {
                handler.handle(messageExt);
            }
            catch (Exception e) {
                LOGGER.error("Failed to consume update message", e);
                status = ConsumeConcurrentlyStatus.RECONSUME_LATER;
                break;
            }
        }

        return status;
    }
}
