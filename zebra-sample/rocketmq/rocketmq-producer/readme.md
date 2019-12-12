
# 准备

如果对应的 RokcetMQ 没有开启 Topic 自动创建，那么先手工到 RocketMQ 新建 Topic，命令如下：

```shell script
./mqadmin updateTopic -n nameserverIp:nameserverPort  -b brokerIp:brokerPort  -t com-guosen-zebra-sample-rocketmq-producer
```

# 配置

## 配置名称

```text
com.guosen.zebra.sample.rocketmq.producer
```

## 配置内容

```properties
server.port=8081
zebra.rocketmq.namesrvAddr=nameserverIp:nameserverPort
zebra.rocketmq.producerGroupName=com-guosen-zebra-sample-rocketmq-producer
zebra.rocketmq.producerInstanceName=zebraRocketMQProducer
```

