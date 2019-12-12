package com.guosen.zebra.cache.change.publisher;

import com.guosen.zebra.cache.change.UpdateMessage;

/**
 * 缓存变更消息发布接口
 */
public interface CacheChangePublisher {

    /**
     * 发布缓存变更消息
     * @param message   缓存变更消息
     */
    void publish(UpdateMessage message);
}
