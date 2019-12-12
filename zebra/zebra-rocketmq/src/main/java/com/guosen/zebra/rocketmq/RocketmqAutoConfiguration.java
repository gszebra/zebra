package com.guosen.zebra.rocketmq;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.guosen.zebra.rocketmq.conf.RocketmqProperties;
import com.guosen.zebra.rocketmq.event.RocketmqEvent;

@Configuration
@EnableConfigurationProperties(RocketmqProperties.class)
@ConditionalOnProperty(prefix = RocketmqProperties.PREFIX, value = "namesrvAddr")
public class RocketmqAutoConfiguration {
	private static final Logger log = LogManager.getLogger(RocketmqAutoConfiguration.class);
	@Autowired
	private RocketmqProperties properties;
	@Autowired
	private ApplicationEventPublisher publisher;
//	@Autowired
//	private GrpcProperties grpcProperties;

	private static boolean isFirstSub = true;

	private static long startTime = System.currentTimeMillis();

	/**
	 * 初始化向rocketmq发送普通消息的生产者
	 * 
	 * @throws Exception
	 */
	@Bean
	@ConditionalOnProperty(prefix = RocketmqProperties.PREFIX, value = "producerInstanceName")
//	@ConditionalOnBean(EtcdClient.class)
	public DefaultMQProducer defaultProducer() throws Exception {
		/**
		 * 一个应用创建一个Producer，由应用来维护此对象，可以设置为全局对象或者单例<br>
		 * 注意：ProducerGroupName需要由应用来保证唯一<br>
		 * ProducerGroup这个概念发送普通的消息时，作用不大，但是发送分布式事务消息时，比较关键，
		 * 因为服务器会回查这个Group下的任意一个Producer
		 */
		DefaultMQProducer producer = new DefaultMQProducer(properties.getProducerGroupName());
		producer.setNamesrvAddr(properties.getNamesrvAddr());
		producer.setInstanceName(properties.getProducerInstanceName());
		producer.setVipChannelEnabled(false);
		producer.setRetryTimesWhenSendAsyncFailed(10);
		producer.setRetryTimesWhenSendFailed(10);
		/**
		 * Producer对象在使用之前必须要调用start初始化，初始化一次即可<br>
		 * 注意：切记不可以在每次发送消息时，都调用start方法
		 */
		producer.start();
		log.info("RocketMq defaultProducer Started.");
//		pubRegister(properties.getProducerGroupName());
		return producer;
	}

	/**
	 * 初始化向rocketmq发送事务消息的生产者
	 * 
	 * @throws Exception
	 */
	@Bean
	@ConditionalOnProperty(prefix = RocketmqProperties.PREFIX, value = "producerTranInstanceName")
//	@ConditionalOnBean(EtcdClient.class)
	public TransactionMQProducer transactionProducer() throws Exception {
		/**
		 * 一个应用创建一个Producer，由应用来维护此对象，可以设置为全局对象或者单例<br>
		 * 注意：ProducerGroupName需要由应用来保证唯一<br>
		 * ProducerGroup这个概念发送普通的消息时，作用不大，但是发送分布式事务消息时，比较关键，
		 * 因为服务器会回查这个Group下的任意一个Producer
		 */
		TransactionMQProducer producer = new TransactionMQProducer(properties.getTransactionProducerGroupName());
		producer.setNamesrvAddr(properties.getNamesrvAddr());
		producer.setInstanceName(properties.getProducerTranInstanceName());
		producer.setRetryTimesWhenSendAsyncFailed(10);
		producer.setRetryTimesWhenSendFailed(10);
		TransactionListener transactionListener = new TransactionListenerImpl();
		ExecutorService executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("client-transaction-msg-check-thread");
                return thread;
            }
        });
		producer.setVipChannelEnabled(false);
		producer.setExecutorService(executorService);
        producer.setTransactionListener(transactionListener);

		/**
		 * Producer对象在使用之前必须要调用start初始化，初始化一次即可<br>
		 * 注意：切记不可以在每次发送消息时，都调用start方法
		 */
		producer.start();

		log.info("RocketMq TransactionMQProducer Started.");
//		pubRegister(properties.getProducerTranInstanceName());
		return producer;
	}

	/**
	 * 初始化rocketmq消息监听方式的消费者
	 * 
	 * @throws Exception
	 */
	@Bean
	@ConditionalOnProperty(prefix = RocketmqProperties.PREFIX, value = "consumerInstanceName")
	public DefaultMQPushConsumer pushConsumer() throws Exception {
		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(properties.getConsumerGroupName());
		consumer.setNamesrvAddr(properties.getNamesrvAddr());
		consumer.setInstanceName(properties.getConsumerInstanceName());
		if (properties.isConsumerBroadcasting()) {
			consumer.setMessageModel(MessageModel.BROADCASTING);
		}
		consumer.setConsumeMessageBatchMaxSize(
				properties.getConsumerBatchMaxSize() == 0 ? 1 : properties.getConsumerBatchMaxSize());// 设置批量消费，以提升消费吞吐量，默认是1
		consumer.setVipChannelEnabled(false);
		/**
		 * 订阅指定topic下tags
		 */
		List<String> subscribeList = properties.getSubscribe();
		for (String subscribe : subscribeList) {
			consumer.subscribe(subscribe.split(":")[0], subscribe.split(":")[1]);
//			subRegister(properties.getConsumerGroupName(), subscribe.split(":")[0]);
		}
		if (properties.isEnableOrderConsumer()) {
			consumer.registerMessageListener((List<MessageExt> msgs, ConsumeOrderlyContext context) -> {
				try {
					context.setAutoCommit(true);
					msgs = filter(msgs);
					if (msgs.size() == 0)
						return ConsumeOrderlyStatus.SUCCESS;
					this.publisher.publishEvent(new RocketmqEvent(msgs, consumer));
				} catch (Exception e) {
					e.printStackTrace();
					return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
				}
				// 如果没有return success，consumer会重复消费此信息，直到success。
				return ConsumeOrderlyStatus.SUCCESS;
			});
		} else {
			consumer.registerMessageListener((List<MessageExt> msgs, ConsumeConcurrentlyContext context) -> {
				try {
					msgs = filter(msgs);
					if (msgs.size() == 0)
						return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
					this.publisher.publishEvent(new RocketmqEvent(msgs, consumer));
				} catch (Exception e) {
					e.printStackTrace();
					return ConsumeConcurrentlyStatus.RECONSUME_LATER;
				}
				// 如果没有return success，consumer会重复消费此信息，直到success。
				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			});
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);// 延迟5秒再启动，主要是等待spring事件监听相关程序初始化完成，否则，回出现对RocketMQ的消息进行消费后立即发布消息到达的事件，然而此事件的监听程序还未初始化，从而造成消息的丢失
					/**
					 * Consumer对象在使用之前必须要调用start初始化，初始化一次即可<br>
					 */
					try {
						consumer.start();
					} catch (Exception e) {
						log.info("RocketMq pushConsumer Start failure!!!.");
						log.error(e.getMessage(), e);
					}
					log.info("RocketMq pushConsumer Started.");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}).start();

		return consumer;
	}

	private List<MessageExt> filter(List<MessageExt> msgs) {
		if (isFirstSub&& !properties.isEnableHisConsumer()) {
			msgs = msgs.stream().filter(item -> startTime - item.getBornTimestamp() < 0).collect(Collectors.toList());
		}
		if (isFirstSub && msgs.size() > 0) {
			isFirstSub = false;
		}
		return msgs;
	}

//	/**
//	 * @throws Exception
//	 * 			@Title: subRegister @Description: sub注册到etcd上 @param
//	 *             设定文件 @return void 返回类型 @throws
//	 */
//	private void subRegister(String subGroupName, String topic) throws Exception {
//		String clientUuid = StringUtils.isEmpty(ZebraRun.APP_NODE) ? UUID.randomUUID() + "" : ZebraRun.APP_NODE;
//		String addrs = grpcProperties == null ? NetUtils.getLocalHost()
//				: NetUtils.getLocalHost() + ":"+grpcProperties.getPort();
//		Map<String, Object> keyValue = Maps.newHashMap();
//		String subKey = ZebraConstants.ETCD_SERVICE_PRE + ZebraConstants.PATH_SEPARATOR + ZebraConstants.DEFAULT_GROUP
//				+ ZebraConstants.PATH_SEPARATOR + topic + ZebraConstants.PATH_SEPARATOR + ZebraConstants.DEFAULT_VERSION
//				+ ZebraConstants.PATH_SEPARATOR + ZebraConstants.DEFAULT_SET + ZebraConstants.PATH_SEPARATOR
//				+ ZebraConstants.TYPE_SUB + ZebraConstants.PATH_SEPARATOR + clientUuid;
//		String ttl = ZebraConstants.ZEBRA_ETCD_CUST_TTL;
//		keyValue.put(ZebraConstants.KEY, subKey);
//		keyValue.put(ZebraConstants.VALUE, subGroupName + ":" + addrs);
//		keyValue.put(ZebraConstants.TTL, ttl);
//		if(EtcdClient.getSingleton().client ==null){
//			EtcdClient.getSingleton().setGrpcProperties(grpcProperties);
//			EtcdClient.getSingleton().init();
//		}
//		EtcdClient.getSingleton().command(EtcdClient.PUT_CMD, keyValue, null, null);
//	}

//	/**
//	 * @throws Exception
//	 * 			@Title: pubRegister @Description: pub注册到etcd上 @param
//	 *             设定文件 @return void 返回类型 @throws
//	 */
//	private void pubRegister(String pubGroupName) throws Exception {
//		String serverUuid = StringUtils.isEmpty(ZebraRun.APP_NODE) ? UUID.randomUUID() + "" : ZebraRun.APP_NODE;
//		String addrs = grpcProperties == null ? NetUtils.getLocalHost()
//				: NetUtils.getLocalHost() + ":"+ grpcProperties.getPort();
//		Map<String, Object> keyValue = Maps.newHashMap();
//		String key = ZebraConstants.ETCD_SERVICE_PRE + ZebraConstants.PATH_SEPARATOR + ZebraConstants.DEFAULT_GROUP
//				+ ZebraConstants.PATH_SEPARATOR + pubGroupName + ZebraConstants.PATH_SEPARATOR
//				+ ZebraConstants.DEFAULT_VERSION + ZebraConstants.PATH_SEPARATOR + ZebraConstants.DEFAULT_SET
//				+ ZebraConstants.PATH_SEPARATOR + ZebraConstants.TYPE_PUB + ZebraConstants.PATH_SEPARATOR + serverUuid;
//		keyValue.put(ZebraConstants.KEY, key);
//		keyValue.put(ZebraConstants.VALUE, addrs);
//		if(EtcdClient.getSingleton().client ==null){
//			EtcdClient.getSingleton().setGrpcProperties(grpcProperties);
//			EtcdClient.getSingleton().init();
//		}
//		EtcdClient.getSingleton().command(EtcdClient.PUT_CMD, keyValue, null, null);
//	}

}
