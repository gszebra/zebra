package com.guosen.zebra.cache.builder;

import com.guosen.zebra.cache.LocalRedisCacheManager;
import com.guosen.zebra.cache.ZebraCacheConfig;
import com.guosen.zebra.cache.change.consumer.RocketMqConsumerAdapter;
import com.guosen.zebra.cache.change.handler.rocketmq.FirstCacheUpdateMessageHandler;
import com.guosen.zebra.cache.change.handler.rocketmq.RocketMqMessageHandler;
import com.guosen.zebra.cache.change.handler.rocketmq.SecondaryCacheUpdateMessageHandler;
import com.guosen.zebra.cache.change.listener.CacheChangeRocketMqListener;
import com.guosen.zebra.cache.common.RocketMqGroupNameNormalizier;
import com.guosen.zebra.core.util.ServiceNameUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RocketMqConsumerAdaptor工厂
 */
public final class RocketMqConsumerAdaptorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMqConsumerAdaptorFactory.class);

    private RocketMqConsumerAdaptorFactory(){}

    public static RocketMqConsumerAdapter create(ZebraCacheConfig zebraCacheConfig, LocalRedisCacheManager cacheManager) {
        DefaultMQPushConsumer consumerForFirstCache = getConsumerForFirstCache(zebraCacheConfig, cacheManager);
        DefaultMQPushConsumer secondaryCacheConsumer = getConsumerForSecondaryCache(zebraCacheConfig, cacheManager);
        return new RocketMqConsumerAdapter(consumerForFirstCache, secondaryCacheConsumer);
    }

    private static DefaultMQPushConsumer getConsumerForFirstCache(ZebraCacheConfig zebraCacheConfig, LocalRedisCacheManager cacheManager) {
        RocketMqMessageHandler updateMessageHandler = new FirstCacheUpdateMessageHandler(cacheManager);
        CacheChangeRocketMqListener cacheChangeRocketMqListener = new CacheChangeRocketMqListener(updateMessageHandler);

        String serviceName = ServiceNameUtil.getServiceName();
        String groupName = "first-cache-" + RocketMqGroupNameNormalizier.normalize(serviceName);
        String instanceName = "first-cache-consumer";

        LOGGER.info("Zebra cache mq consumer, group name : {}, instance name : {}", groupName, instanceName);

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(zebraCacheConfig.getRocketMqNameSrv());
        consumer.setMessageModel(MessageModel.BROADCASTING); // 必须设置为广播形式，每个微服务都要跟新一级缓存
        consumer.setInstanceName(instanceName);
        consumer.setMessageListener(cacheChangeRocketMqListener);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP); // 重启所有缓存一级都失效，无需消费老的消息。

        return consumer;
    }

    private static DefaultMQPushConsumer getConsumerForSecondaryCache(ZebraCacheConfig zebraCacheConfig, LocalRedisCacheManager cacheManager) {
        RocketMqMessageHandler updateMessageHandler = new SecondaryCacheUpdateMessageHandler(cacheManager);
        CacheChangeRocketMqListener cacheChangeRocketMqListener = new CacheChangeRocketMqListener(updateMessageHandler);

        String serviceName = ServiceNameUtil.getServiceName();
        String groupName = "secondary-cache-" + RocketMqGroupNameNormalizier.normalize(serviceName);
        String instanceName = "secondary-cache-consumer";

        LOGGER.info("Zebra cache mq consumer, group name : {}, instance name : {}", groupName, instanceName);

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(zebraCacheConfig.getRocketMqNameSrv());
        consumer.setMessageModel(MessageModel.CLUSTERING); // 设置为cluster模式，只有一个微服务实例更新二级缓存
        consumer.setInstanceName(instanceName);
        consumer.setMessageListener(cacheChangeRocketMqListener);

        return consumer;
    }
}
