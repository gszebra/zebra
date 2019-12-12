package com.guosen.zebra.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * Spring ApplicationContext 工具类
 */
@Component("applicationContextUtil")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApplicationContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextUtil.applicationContext = applicationContext;
    }

    /**
     * 返回ApplicationContext关联的Environment
     */
    public static Environment getEnvironment() {
        return applicationContext.getEnvironment();
    }

    /**
     * 根据bean名称获取spring的bean
     * @param name bean名称
     * @return 对应的bean
     * @throws BeansException
     */
    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> requiredType) {
        return applicationContext.getBean(name, requiredType);
    }

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(requiredType);
    }
    
    @SuppressWarnings("rawtypes")
	public static List<Object> getTypedBeansWithAnnotation(String serviceName ,Class<? extends Annotation> annotationType) {
		List<String> names = Arrays.asList(applicationContext.getBeanNamesForAnnotation(annotationType));
		return names.stream().filter(name ->{
			Annotation an = applicationContext.findAnnotationOnBean(name, annotationType);
			Object server = applicationContext.getBean(name);
			InvocationHandler handler = Proxy.getInvocationHandler(an);
			Field hField;
			try {
				hField = handler.getClass().getDeclaredField("memberValues");
				hField.setAccessible(true);
				Map memberValues =(Map)hField.get(handler);
				String service = (String) memberValues.get("service");
				if (StringUtils.isEmpty(service)) {
					Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(server.getClass());
					for (Class<?> interfaceClass : interfaces) {
						String interfaceName = interfaceClass.getName();
						if (!StringUtils.startsWith(interfaceName, "org.springframework")
								&& !StringUtils.startsWith(interfaceName, "java.")) {
							service = interfaceName;
						}
					}
				}
				return serviceName.equals(service);
			} catch (Exception e) {
				return false;
			}
		}).map(name-> applicationContext.getBean(name)).collect(Collectors.toList());
	}
    
	public static List<Object> getTypedBeansWithAnnotation(Class<? extends Annotation> annotationType) {
		List<String> names = Arrays.asList(applicationContext.getBeanNamesForAnnotation(annotationType));
		return names.stream().map(name-> applicationContext.getBean(name)).collect(Collectors.toList());
	}
}
