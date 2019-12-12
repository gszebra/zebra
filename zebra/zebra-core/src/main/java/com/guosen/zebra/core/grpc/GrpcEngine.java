package com.guosen.zebra.core.grpc;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guosen.zebra.core.boot.autoconfigure.GrpcProperties;
import com.guosen.zebra.core.common.NamedThreadFactory;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.exception.RpcFrameworkException;
import com.guosen.zebra.core.grpc.client.GrpcClientStrategy;
import com.guosen.zebra.core.grpc.client.GrpcProtocolClient;
import com.guosen.zebra.core.grpc.interceptor.HeaderClientInterceptor;
import com.guosen.zebra.core.grpc.interceptor.HeaderServerInterceptor;
import com.guosen.zebra.core.grpc.interceptor.MonitoringServerInterceptor;
import com.guosen.zebra.core.grpc.server.GrpcServerStrategy;
import com.guosen.zebra.core.grpc.util.PropertiesContent;
import com.guosen.zebra.core.grpc.util.SslUtil;
import com.guosen.zebra.core.monitor.health.Health;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;
import com.guosen.zebra.core.serializer.utils.CollectionUtils;
import com.guosen.zebra.core.serializer.utils.ReflectUtils;
import com.guosen.zebra.monitor.metrics.Configuration;
import com.guosen.zebra.sentinel.interceptor.ZebraSentinelClientInterceptor;
import com.guosen.zebra.sentinel.interceptor.ZebraSentinelServerInterceptor;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.Internal;
import io.grpc.ManagedChannel;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.util.TransmitStatusRuntimeExceptionInterceptor;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * @ClassName: GrpcEngine
 * @Description: 生成grpcserver也进行client的连接池初始化
 * @author 邓启翔
 * @date 2017年10月31日 下午12:59:12
 * 
 */
@Internal
public final class GrpcEngine {
	private static final Logger log = LogManager.getLogger(GrpcEngine.class);
	private GrpcProperties grpcProperties;

	private final EtcdRegistry registry;

	private GenericKeyedObjectPool<String, Channel> channelPool;
	
	private static Executor ZebraBizExecutor;
	
	private static NioEventLoopGroup bossEventLoop;
	
	private static Map<String, NioEventLoopGroup> workEventLoop = Maps.newConcurrentMap();

	private final Map<String, Map<String, Object>> subscribeServiceCache = Maps.newConcurrentMap();
	
	private static ThreadPoolTaskExecutor orgExecutor;
	
	public GrpcEngine() {
		registry = new EtcdRegistry();
	}

	public GrpcEngine(GrpcProperties grpcProperties) {
		registry = new EtcdRegistry();
		registry.setGrpcProperties(grpcProperties);
		this.grpcProperties = grpcProperties;
		initBizExecutor();
	}

	private synchronized void initBizExecutor() {
		if (ZebraBizExecutor != null)
			return;
		orgExecutor = new ThreadPoolTaskExecutor();
		orgExecutor.setBeanName(ZebraConstants.EXECUTOR_NAME);
		if (grpcProperties.getMaxPoolSize() != 0) {
			orgExecutor.setMaxPoolSize(grpcProperties.getMaxPoolSize());
		} else {
			orgExecutor.setMaxPoolSize(ZebraConstants.MAX_POOL_SIZE);
		}
		if (grpcProperties.getCorePoolSize() != 0) {
			orgExecutor.setCorePoolSize(grpcProperties.getCorePoolSize());
		} else {
			orgExecutor.setCorePoolSize(ZebraConstants.CORE_POOL_SIZE);
		}
		if (grpcProperties.getQueueCapacity() != 0) {
			orgExecutor.setQueueCapacity(grpcProperties.getQueueCapacity());
		} else {
			orgExecutor.setQueueCapacity(ZebraConstants.QUEUE_CAPACITY);
		}
		orgExecutor.setDaemon(false);
		orgExecutor.initialize();
		ZebraBizExecutor = TtlExecutors.getTtlExecutor(orgExecutor);
		ZebraBizExecutor.execute(new Runnable() {
			@Override
			public void run() {
				System.err.println("<<<<zebra线程池初始化成功!>>>>");
			}
		});
	}
	private synchronized void initChannelPool() {
		if(channelPool ==null){
			GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
			config.setMaxTotal(Integer.MAX_VALUE);
			config.setMaxTotalPerKey(10);
			config.setBlockWhenExhausted(true);
			config.setMinIdlePerKey(3);
			config.setMaxIdlePerKey(10);
			config.setMaxWaitMillis(3000L);
			config.setNumTestsPerEvictionRun(Integer.MAX_VALUE);
			config.setTestOnBorrow(true);
			config.setTestOnReturn(false);
			config.setTestWhileIdle(true);
			config.setTimeBetweenEvictionRunsMillis(1000 * 60 * 5);
//			config.setMinEvictableIdleTimeMillis(1000 * 24 * 60 * 60000L);
//			config.setMinEvictableIdleTimeMillis(1 * 60000L);
			this.channelPool = new GenericKeyedObjectPool<String, Channel>(new GrpcChannelFactory(), config);
		}
	}

	public synchronized Object getClient(Map<String, Object> params) throws Exception {
		if (channelPool == null) {
			initChannelPool();
		}
		GrpcProtocolClient.ChannelCall channelCall = new GrpcProtocolClient.ChannelCall() {
			@Override
			public Channel borrowChannel(Map<String, Object> relParams,String serviceName) {
				try {
					if (relParams == null) {
						relParams = params;
					}
					String serviceId = cacheSubscribeUrl(relParams,serviceName);
//					log.debug("borrowChannel key = {}, ide = {}, activtie ={}, max ={}", serviceId,
//							channelPool.getNumIdle(serviceId), channelPool.getNumActive(serviceId),channelPool.getMaxTotalPerKey());
					return channelPool.borrowObject(serviceId);
				} catch (Exception e) {
					throw new java.lang.IllegalArgumentException("Grpc borrow Channel failed", e);
				}
			}

			private String cacheSubscribeUrl(Map<String, Object> relParams, String serviceName) {
				String group = (String) relParams.get(ZebraConstants.GROUP_KEY);
				String version = (String) relParams.get(ZebraConstants.VERSION_KEY);
				String set = (String) relParams.get(ZebraConstants.KEY_SET);
				String addr = (String) relParams.get(ZebraConstants.KEY_ADDR);
				String key = group + ZebraConstants.PATH_SEPARATOR + serviceName + ZebraConstants.PATH_SEPARATOR
						+ version + ZebraConstants.PATH_SEPARATOR + set + ZebraConstants.PATH_SEPARATOR + addr;
				if (subscribeServiceCache.get(key) == null) {
					Map<String, Object> subscribe = new HashMap<String, Object>(relParams);
					subscribe.put(ZebraConstants.SERVICE_NAME, serviceName);
					subscribeServiceCache.put(key, subscribe);
				}
				return key;
			}

			@Override
			public void returnChannel(Map<String, Object> relParams,String serviceName, final Channel channel) {
				if (relParams == null) {
					relParams = params;
				}
				String group = (String) relParams.get(ZebraConstants.GROUP_KEY);
				String version = (String) relParams.get(ZebraConstants.VERSION_KEY);
				String set = (String) relParams.get(ZebraConstants.KEY_SET);
				String addr = (String) relParams.get(ZebraConstants.KEY_ADDR);
				String key = group + ZebraConstants.PATH_SEPARATOR + serviceName + ZebraConstants.PATH_SEPARATOR
						+ version + ZebraConstants.PATH_SEPARATOR + set + ZebraConstants.PATH_SEPARATOR + addr;
				//log.debug("returnChannel key = {}", key);
				channelPool.returnObject(key, channel);
			}

		};
		GrpcClientStrategy strategy = new GrpcClientStrategy(params, channelCall);
		return strategy.getGrpcClient();
	}

	public io.grpc.Server getServer(List<RpcServiceBaseInfo> rpcSeviceList, GrpcProperties grpcProperties,
			AbstractApplicationContext applicationContext) throws Exception {
		this.setGrpcProperties(grpcProperties);
		int maxMessageSize = GrpcUtil.DEFAULT_MAX_MESSAGE_SIZE;
		if(PropertiesContent.getIntValue(ZebraConstants.MESSAGE_SIZE_KEY, 0)!=0){
			maxMessageSize = PropertiesContent.getIntValue(ZebraConstants.MESSAGE_SIZE_KEY,maxMessageSize);
		}
		ServerBuilder<?> build = NettyServerBuilder.forPort(grpcProperties.getPort())
//												   .sslContext(buildServerSslContext())
												   .bossEventLoopGroup(createBossEventLoopGroup())
												   .workerEventLoopGroup(createWorkEventLoopGroup("localServer"))
												   .maxInboundMessageSize(maxMessageSize)
												   .executor(ZebraBizExecutor);//spring线程池

        MonitoringServerInterceptor monitoringInterceptor = MonitoringServerInterceptor.create(Configuration.allMetrics());
		for (RpcServiceBaseInfo conf : rpcSeviceList) {
			List<ServerInterceptor> interceptors = Lists.newArrayList();
			interceptors.add(HeaderServerInterceptor.instance());
			interceptors.add(TransmitStatusRuntimeExceptionInterceptor.instance());
			interceptors.add(monitoringInterceptor);
			interceptors.add(new ZebraSentinelServerInterceptor());
			if(CollectionUtils.isNotEmpty(conf.getClzs())){
				for(Class<?> clz : conf.getClzs()){
					ServerInterceptor inerceptor = (ServerInterceptor) ReflectUtils.classInstance(clz);
					interceptors.add(inerceptor);
				}
			}
			Object protocolImpl = conf.getTarget();
			GrpcServerStrategy strategy = new GrpcServerStrategy(conf, protocolImpl);
			List<ServerServiceDefinition> defList = strategy.getServerDefintion();
			for(ServerServiceDefinition item :defList){//每一个service都导出3个定义，一个是标准grpc，一个是json、一个是inputstream
				ServerServiceDefinition serviceDefinition = ServerInterceptors.intercept(item,interceptors);
				build.addService(serviceDefinition);
			}
			registry.setGrpcProperties(grpcProperties);
			registry.register(conf, grpcProperties.getPort());
		}
		//绑定health
		Health healthInstance = new Health(applicationContext);
        BeanDefinitionRegistry beanDefinitonRegistry =(BeanDefinitionRegistry) applicationContext.getBeanFactory();
        BeanDefinitionBuilder beanDefinitionBuilder =BeanDefinitionBuilder.genericBeanDefinition(Health.class);
        beanDefinitonRegistry.registerBeanDefinition(Health.class.getName(),beanDefinitionBuilder.getRawBeanDefinition());
        applicationContext.getBeanFactory().registerSingleton(Health.class.getName(),healthInstance);
        build.addService(healthInstance.bindService());//将health添加到grpc里面
		log.info("grpc server is build complete ");
		return build.build();
	}

	public GrpcProperties getGrpcProperties() {
		return grpcProperties;
	}

	public void setGrpcProperties(GrpcProperties grpcProperties) {
		this.grpcProperties = grpcProperties;
	}
	
	@SuppressWarnings("unused")
	private SslContext buildServerSslContext() {
		try {
			InputStream certs = SslUtil.loadInputStreamCert("server.pem");
			InputStream keys = SslUtil.loadInputStreamCert("server_pkcs8.key");
			X509Certificate serverTrustedCaCerts = SslUtil.loadX509Cert("server.pem");
			return GrpcSslContexts.configure(SslContextBuilder.forServer(certs, keys).trustManager(serverTrustedCaCerts)
					.clientAuth(ClientAuth.REQUIRE)).build();
		} catch (Exception e) {
			throw new RpcFrameworkException(e);
		}
	}
	@SuppressWarnings("unused")
	private SslContext buildClientSslContext() {
		try {
			return GrpcSslContexts
					.configure(SslContextBuilder.forClient()//
							.keyManager(SslUtil.loadFileCert("server.pem"), SslUtil.loadFileCert("server_pkcs8.key"))
							.trustManager(SslUtil.loadX509Cert("server.pem")))//
					.build();
		} catch (Exception e) {
			throw new RpcFrameworkException(e);
		}
	}

	private class GrpcChannelFactory extends BaseKeyedPooledObjectFactory<String, Channel> {

		private final List<ClientInterceptor> interceptors = Arrays.asList(HeaderClientInterceptor.instance(),new ZebraSentinelClientInterceptor());

		@Override
		public Channel create(String key) throws Exception {
			Map<String, Object> subscribe = subscribeServiceCache.get(key);
			Class<?>[] clzs = (Class<?>[]) subscribe.get(ZebraConstants.KEY_CLIENT_INTERCEPTOR);
			if (CollectionUtils.isNotEmpty(clzs)) {
				for (Class<?> clz : clzs) {
					ClientInterceptor inerceptor = (ClientInterceptor) ReflectUtils.classInstance(clz);
					interceptors.add(inerceptor);
				}
			}
			int maxMessageSize = GrpcUtil.DEFAULT_MAX_MESSAGE_SIZE;
			if(PropertiesContent.getIntValue(ZebraConstants.MESSAGE_SIZE_KEY, 0)!=0){
				maxMessageSize = PropertiesContent.getIntValue(ZebraConstants.MESSAGE_SIZE_KEY,maxMessageSize);
			}
			Channel channel = NettyChannelBuilder.forTarget(getGrpcProperties().getRegistryAddress())
					.nameResolverFactory(new GrpcNameResolverProvider(subscribe))//
					.defaultLoadBalancingPolicy("round_robin")
					.eventLoopGroup(createWorkEventLoopGroup(key))//
					// .sslContext(buildClientSslContext())
					.negotiationType(NegotiationType.PLAINTEXT)
					.maxInboundMessageSize(maxMessageSize)
					.intercept(interceptors).build();
			return channel;
		}

		@Override
		public PooledObject<Channel> wrap(Channel value) {
			return new DefaultPooledObject<Channel>(value);
		}

		@Override
		public void destroyObject(String key, PooledObject<Channel> p) throws Exception {
			channelPool.clear(key);
			try {
				((ManagedChannel) p.getObject()).shutdown().awaitTermination(5, TimeUnit.SECONDS);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw e;
			}
		}

		@Override
		public boolean validateObject(String key, PooledObject<Channel> p) {
			boolean ret = false;
			ConnectivityState state = ((ManagedChannel) p.getObject()).getState(true);
			switch (state) {
			case CONNECTING:
				ret = true;
				break;
			case READY:
				ret = true;
				break;
			case IDLE:
				ret = true;
				break;
			default:
				ret = false;
				break;
			}
			return ret;
		}

		// after borrow Channel
		@Override
		public void activateObject(String key, PooledObject<Channel> p) throws Exception {
			super.activateObject(key, p);
		}

		// after return Channel
		@Override
		public void passivateObject(String key, PooledObject<Channel> p) throws Exception {
			super.passivateObject(key, p);
		}
	}

	private synchronized NioEventLoopGroup createBossEventLoopGroup() {
		if(bossEventLoop==null){
			ThreadFactory threadFactory = new NamedThreadFactory("grpc-default-boss-ELG", false);
			bossEventLoop =new NioEventLoopGroup(1, Executors.newCachedThreadPool(threadFactory));
		}
		return bossEventLoop;
	}

	private synchronized NioEventLoopGroup createWorkEventLoopGroup(String key) {
		if (workEventLoop.get(key) == null) {
			ThreadFactory threadFactory = new NamedThreadFactory("grpc-default-worker-ELG", false);
			NioEventLoopGroup eventLoop = new NioEventLoopGroup(0, Executors.newCachedThreadPool(threadFactory));
			workEventLoop.put(key, eventLoop);
		}
		return workEventLoop.get(key);
	}
	
	public static int getActiviteThreadCount(){
		return orgExecutor.getActiveCount();
	}
}
