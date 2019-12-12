package com.guosen.zebra.cache.change.handler.common;

import com.guosen.zebra.cache.LocalRedisCacheManager;
import com.guosen.zebra.cache.ZebraCache;
import com.guosen.zebra.cache.change.UpdateMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirstCacheHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FirstCacheHandler.class);

    private LocalRedisCacheManager cacheManager;

    public FirstCacheHandler(LocalRedisCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void handle(UpdateMessage updateMessage) {
        String cacheName = updateMessage.getCacheName();
        if (StringUtils.isBlank(cacheName)) {
            // 这种情况只有老的消息才会有，但是只有在升级过程中才可能出现，部分实例升级，部分实例未升级（未升级的没有cacheName）
            LOGGER.error("Cache name is empty, message : {}", updateMessage);
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Begin handle update message {} ",  updateMessage);
        }

        ZebraCache zebraCache = (ZebraCache) cacheManager.getCache(cacheName);
        if (zebraCache == null) {
            LOGGER.error("Could not find cache name of {}", cacheName);
            return;
        }

        zebraCache.handleForLocalCache(updateMessage);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Finish handle update message {}", updateMessage);
        }
    }
}
