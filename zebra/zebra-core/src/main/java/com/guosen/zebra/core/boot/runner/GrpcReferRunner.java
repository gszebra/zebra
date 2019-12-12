package com.guosen.zebra.core.boot.runner;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.support.AbstractApplicationContext;

import com.google.common.collect.Maps;
import com.guosen.zebra.core.boot.autoconfigure.GrpcProperties;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.anotation.AnnotationAutoConf;
import com.guosen.zebra.core.grpc.anotation.ZebraReference;
import com.guosen.zebra.core.grpc.client.GrpcReference;
import com.guosen.zebra.core.grpc.server.GenericService;

import io.grpc.stub.AbstractStub;

/**
 * @ClassName: GrpcServiceRunner
 * @Description: spring boot 启动，扫描service对GRPC服务进行注册
 * @author 邓启翔
 * @date 2017年10月30日 下午4:41:31
 * 
 */
public class GrpcReferRunner extends InstantiationAwareBeanPostProcessorAdapter {
	private static final Logger logger = LogManager.getLogger(GrpcReferRunner.class);
	@Autowired
	private AbstractApplicationContext applicationContext;
	private final Map<String, Object> serviceBean = Maps.newConcurrentMap();
	private GrpcProperties grpcProperties;

	public GrpcReferRunner(GrpcProperties grpcProperties) {
		System.err.println(">>>>>>>>>>>>>>>服务启动执行，开始自动发现服务<<<<<<<<<<<<<");
		this.grpcProperties = grpcProperties;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Class<?> searchType = bean.getClass();
		while (!Object.class.equals(searchType) && searchType != null) {
			Field[] fields = searchType.getDeclaredFields();
			for (Field field : fields) {
				ZebraReference reference = field.getAnnotation(ZebraReference.class);
				if (reference != null) {
					AnnotationAutoConf.autoConf(reference);//注解属性动态配置
					logger.debug("**************reference begin,reference={}",reference);
					Object value = null;
					try {
						value = applicationContext.getBean(field.getType());
					} catch (NoSuchBeanDefinitionException e) {
						value = null;
					}
					if (value == null) {
						value = assembleRemoteSeviceObj(reference, field.getType());
					}
					try {
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						field.set(bean, value);
					} catch (Throwable e) {
						logger.error("Failed to init remote service reference at filed " + field.getName()
								+ " in class " + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
					}
				}
			}
			searchType = searchType.getSuperclass();
		}
		return bean;
	}

	/** 
	* @Title: assembleRemoteSeviceObj 
	* @Description: 组装远程服务代理类
	* @param @param reference
	* @param @param referenceClass
	* @param @return    设定文件 
	* @return Object    返回类型 
	* @throws 
	*/
	private Object assembleRemoteSeviceObj(ZebraReference reference, Class<?> referenceClass) {
		String serviceName = this.getServiceName(reference, referenceClass);
		if(reference.async()){
			serviceName = serviceName+ZebraConstants.ASYNC_KEY;
		}
		if (serviceBean.containsKey(serviceName) && !isGrpcStubClient(referenceClass)
				&& !isGenericClient(referenceClass)) {
			return serviceBean.get(serviceName);
		} else {
			GrpcReference ref = new GrpcReference();
			ref.setGrpcProperties(grpcProperties);
			Object bean = ref.getProxyObj(reference, referenceClass);
			serviceBean.putIfAbsent(serviceName, bean);
			return bean;
		}
	}


	

	private String getServiceName(ZebraReference reference, Class<?> referenceClass) {
		String serviceName = reference.service();
		if (StringUtils.isBlank(serviceName)) {
			if (this.isGrpcStubClient(referenceClass)) {
				throw new java.lang.IllegalArgumentException("reference service can not be null or empty");
			} else {
				serviceName = referenceClass.getName();
			}
		}
		return serviceName;
	}
	private boolean isGrpcStubClient(Class<?> referenceClass) {
		if (AbstractStub.class.isAssignableFrom(referenceClass)) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isGenericClient(Class<?> referenceClass) {
		if (GenericService.class.isAssignableFrom(referenceClass)) {
			return true;
		} else {
			return false;
		}
	}
}
