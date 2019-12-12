package com.guosen.zebra.cache.change.publisher;

import com.guosen.zebra.cache.change.UpdateMessage;

/**
 * NoneOp缓存变更消息发布器
 */
public class CacheChangeNoneOpPublisher implements CacheChangePublisher {
    @Override
    public void publish(UpdateMessage message) {
        // 不做任何事情
    }
}
