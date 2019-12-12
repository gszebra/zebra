package com.guosen.zebra.cache.builder;

import com.guosen.zebra.cache.ZebraCacheConfig;
import com.guosen.zebra.cache.common.RocketMqGroupNameNormalizier;
import com.guosen.zebra.core.util.ServiceNameUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultMQProducer工厂
 */
public class DefaultMQProducerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMQProducerFactory.class);

    /**
     * 分布式缓存Rocket MQ producer实例名称
     */
    private static final String DC_MQ_SYNC_PRODUCER_INSTANCE_NAME = "dc_sync_producer";

    /**
     * 构建DefaultMQProducer工厂
     * @param zebraCacheConfig  缓存配置
     * @return  DefaultMQProducer工厂
     */
    public static DefaultMQProducer create(ZebraCacheConfig zebraCacheConfig) {
        String serviceName = ServiceNameUtil.getServiceName();
        String groupName = RocketMqGroupNameNormalizier.normalize(serviceName);

        LOGGER.info("Zebra cache mq producer, group name : {}, instance name : {}", groupName, DC_MQ_SYNC_PRODUCER_INSTANCE_NAME);

        DefaultMQProducer producer = new DefaultMQProducer(groupName);
        producer.setNamesrvAddr(zebraCacheConfig.getRocketMqNameSrv());
        producer.setInstanceName(DC_MQ_SYNC_PRODUCER_INSTANCE_NAME);

        return producer;
    }
}
