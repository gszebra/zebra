package com.guosen.zebra.cache.change.handler.rocketmq;

import com.alibaba.fastjson.JSONObject;
import com.guosen.zebra.cache.LocalRedisCacheManager;
import com.guosen.zebra.cache.change.UpdateMessage;
import com.guosen.zebra.cache.change.handler.common.FirstCacheHandler;
import org.apache.rocketmq.common.message.MessageExt;

import java.nio.charset.StandardCharsets;

public class FirstCacheUpdateMessageHandler implements RocketMqMessageHandler {
    private FirstCacheHandler firstCacheHandler;

    public FirstCacheUpdateMessageHandler(LocalRedisCacheManager cacheManager) {
        firstCacheHandler = new FirstCacheHandler(cacheManager);
    }

    public void handle(MessageExt messageExt) {
        UpdateMessage updateMessage = getUpdateMessage(messageExt);
        firstCacheHandler.handle(updateMessage);
    }

    private UpdateMessage getUpdateMessage(MessageExt messageExt) {
        byte[] rawBody = messageExt.getBody();
        String strBody = new String(rawBody, StandardCharsets.UTF_8);
        return JSONObject.parseObject(strBody, UpdateMessage.class);
    }
}
