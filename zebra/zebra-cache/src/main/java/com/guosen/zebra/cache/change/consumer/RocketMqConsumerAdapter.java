package com.guosen.zebra.cache.change.consumer;

import com.guosen.zebra.cache.common.Constants;
import com.guosen.zebra.core.util.ServiceNameUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Rocket MQ Consumer包装器，用于消费者启动和关闭订阅
 */
public class RocketMqConsumerAdapter implements InitializingBean, DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMqConsumerAdapter.class);

    private DefaultMQPushConsumer firstCacheConsumer;
    private DefaultMQPushConsumer secondaryCacheConsumer;

    public RocketMqConsumerAdapter(DefaultMQPushConsumer firstCacheConsumer, DefaultMQPushConsumer secondaryCacheConsumer) {
        this.firstCacheConsumer = firstCacheConsumer;
        this.secondaryCacheConsumer = secondaryCacheConsumer;
    }

    @Override
    public void afterPropertiesSet() throws MQClientException {
        startConsumer(firstCacheConsumer);
        startConsumer(secondaryCacheConsumer);
    }

    private void startConsumer(DefaultMQPushConsumer consumer) throws MQClientException {
        String serviceName = ServiceNameUtil.getServiceName();
        LOGGER.info("Begin to start zebra cache sync rocket mq consumer, topic : {}, tags : {}",
                Constants.ROCKET_MQ_CACHE_CHANGE_TOPIC,
                serviceName);

        try {
            // 微服务全称就是要订阅的topic对应的tag
            consumer.subscribe(Constants.ROCKET_MQ_CACHE_CHANGE_TOPIC, serviceName);
            consumer.start();
        }
        catch (MQClientException e) {
            LOGGER.error("Failed to start MQ topic, topicName : {}, tagName : {}",
                    Constants.ROCKET_MQ_CACHE_CHANGE_TOPIC, serviceName, e);
            throw e;
        }

        LOGGER.info("Finish to start zebra cache sync rocket mq consumer.");
    }

    @Override
    public void destroy() {
        LOGGER.info("Begin to shutdown zebra cache sync rocket mq consumer.");

        shutDownConsumer(firstCacheConsumer);
        shutDownConsumer(secondaryCacheConsumer);

        LOGGER.info("Finish to shutdown zebra cache sync rocket mq consumer.");
    }

    private void shutDownConsumer(DefaultMQPushConsumer consumer) {
        try {
            consumer.shutdown();
        }
        catch (Exception e) {
            LOGGER.error("Failed to shutdown rocket mq consumer", e);
        }
    }
}
