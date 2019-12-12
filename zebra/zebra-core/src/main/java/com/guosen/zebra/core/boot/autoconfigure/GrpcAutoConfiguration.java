package com.guosen.zebra.core.boot.autoconfigure;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.guosen.zebra.core.boot.runner.GrpcReferRunner;
import com.guosen.zebra.core.boot.runner.GrpcServiceRunner;
import com.guosen.zebra.core.common.ZebraConstants;


/** 
* @ClassName: GrpcAutoConfiguration 
* @Description: 
* @author 邓启翔 
* @date 2017年10月31日 上午8:07:34 
*  
*/
@Configuration
@ConditionalOnProperty(prefix = "zebra.grpc", name = "registryAddress")
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcAutoConfiguration {
	@Autowired
	private GrpcProperties grpcProperties;

	@Bean
	@ConditionalOnProperty(prefix = "zebra.grpc", name = "port")
	public GrpcServiceRunner thrallServiceRunner() {
		return new GrpcServiceRunner();
	}

	@Bean
	public BeanPostProcessor thrallReferenceRunner() {
		return new GrpcReferRunner(grpcProperties);
	}
	
	@Lazy
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	@Bean
	public Executor zebraBizExecutor() {
		ThreadPoolTaskExecutor orgExecutor = new ThreadPoolTaskExecutor();
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
		Executor zebraBizExecutor = TtlExecutors.getTtlExecutor(orgExecutor);
		return zebraBizExecutor;
	}
}
