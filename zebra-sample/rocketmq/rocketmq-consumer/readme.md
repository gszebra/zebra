# 配置

## 配置名称

```text
com.guosen.zebra.sample.rocketmq.consumer
```

## 配置内容

```properties
zebra.rocketmq.namesrvAddr=rocketMqNameServerIp:9876
zebra.rocketmq.consumerGroupName=com-guosen-zebra-sample-rocketmq-consumer
zebra.rocketmq.consumerInstanceName=zebraRocketMQConsumer
zebra.rocketmq.consumerBatchMaxSize=1
zebra.rocketmq.consumerBroadcasting=false
zebra.rocketmq.subscribe[0]=com-guosen-zebra-sample-rocketmq-producer:tagA
zebra.rocketmq.enableHisConsumer=false
zebra.rocketmq.enableOrderConsumer=false
```
