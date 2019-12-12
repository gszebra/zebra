package com.guosen.zebra.cache.change.handler.rocketmq;

import org.apache.rocketmq.common.message.MessageExt;

public interface RocketMqMessageHandler {
    /**
     * 处理缓存变更通知
     * @param messageExt 缓存变更通知
     */
    void handle(MessageExt messageExt);
}
