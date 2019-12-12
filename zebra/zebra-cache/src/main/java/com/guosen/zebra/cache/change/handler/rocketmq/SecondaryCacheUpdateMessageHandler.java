package com.guosen.zebra.cache.change.handler.rocketmq;

import com.alibaba.fastjson.JSONObject;
import com.guosen.zebra.cache.LocalRedisCacheManager;
import com.guosen.zebra.cache.ZebraCache;
import com.guosen.zebra.cache.change.UpdateMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class SecondaryCacheUpdateMessageHandler implements RocketMqMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecondaryCacheUpdateMessageHandler.class);

    /**
     * Rocket MQ 微服务名称的key, 注意和zebra-cache-sync一致
     */
    private static final String PROPERTY_KEY_SERVICE_NAME = "ZEBRA_SERVICE";

    /**
     * 消息超时时间。
     */
    private static final long EXPIRED_TIME_MILLIS = 1800 * 1000;

    private LocalRedisCacheManager cacheManager;

    public SecondaryCacheUpdateMessageHandler(LocalRedisCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void handle(MessageExt messageExt) {
        String sourceServiceName = messageExt.getUserProperty(PROPERTY_KEY_SERVICE_NAME);
        if (StringUtils.isEmpty(sourceServiceName)) {
            // 如果为空，说明是来自本微服务实例的（业务微服务实例的变更消息没有带ZEBRA_SERVICE这个key)
            return;
        }

        long bornTime = messageExt.getBornTimestamp();
        if (System.currentTimeMillis() - bornTime > EXPIRED_TIME_MILLIS) {
            LOGGER.info("Message expired, message id : {}, message born time : {}", messageExt.getMsgId(), bornTime);
            return;
        }

        UpdateMessage updateMessage = getUpdateMessage(messageExt);
        String cacheName = updateMessage.getCacheName();
        if (StringUtils.isBlank(cacheName)) {
            LOGGER.error("Cache name is empty, message : {}", updateMessage);
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Begin handle update message : {} ",  updateMessage);
        }

        ZebraCache zebraCache = (ZebraCache) cacheManager.getCache(cacheName);
        if (zebraCache == null) {
            LOGGER.error("Could not find cache name of {}", cacheName);
            return;
        }

        zebraCache.handleForRedisCache(updateMessage);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Finish handle update message {}", updateMessage);
        }
    }

    private UpdateMessage getUpdateMessage(MessageExt messageExt) {
        byte[] rawBody = messageExt.getBody();
        String strBody = new String(rawBody, StandardCharsets.UTF_8);
        return JSONObject.parseObject(strBody, UpdateMessage.class);
    }
}
