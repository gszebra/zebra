package com.guosen.zebra.core.boot.runner;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.google.common.collect.Sets;
import com.guosen.zebra.ZebraRun;
import com.guosen.zebra.core.common.GrpcAop;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;
import com.guosen.zebra.core.grpc.anotation.AnnotationAutoConf;
import com.guosen.zebra.core.grpc.anotation.ZebraService;

@Order(1)
@Component
public class RpcSeriviceBaseInfoReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcSeriviceBaseInfoReader.class);

    @Autowired
    private AbstractApplicationContext applicationContext;

    private List<RpcServiceBaseInfo> rpcSeriviceBaseInfos;

    public List<RpcServiceBaseInfo> getRpcSeriviceBaseInfos() {
        return rpcSeriviceBaseInfos;
    }

    public void setRpcSeriviceBaseInfos(List<RpcServiceBaseInfo> rpcSeriviceBaseInfos) {
        this.rpcSeriviceBaseInfos = rpcSeriviceBaseInfos;
    }

    @PostConstruct
    public void read() throws Exception {
        rpcSeriviceBaseInfos = doRead();
    }

    private List<RpcServiceBaseInfo> doRead() throws Exception {
        Collection<Object> instances = getTypedBeansWithAnnotation(ZebraService.class);
        if (instances.size() == 0) {
            return null;
        }

        List<RpcServiceBaseInfo> rpcSeviceList = new ArrayList<>();
        for (Object instance : instances) {
            RpcServiceBaseInfo conf = new RpcServiceBaseInfo();
            Object target = GrpcAop.getTarget(instance);
            ZebraService serviceAnnotation = target.getClass().getAnnotation(ZebraService.class);
            AnnotationAutoConf.autoConf(serviceAnnotation);//注解属性动态配置
            String serviceName = serviceAnnotation.service();
            conf.setClzs(serviceAnnotation.interceptors());
            Set<String> serviceNames = Sets.newHashSet();
            if (StringUtils.isBlank(serviceName)) {
                if (this.isGrpcServer(instance)) {
                    throw new java.lang.IllegalArgumentException(
                            "you use grpc stub service,must set service name,service instance is" + instance);
                } else {
                    Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(target.getClass());
                    for (Class<?> interfaceClass : interfaces) {
                        String interfaceName = interfaceClass.getName();
                        if (!StringUtils.startsWith(interfaceName, "org.springframework")
                                && !StringUtils.startsWith(interfaceName, "java.")) {
                            serviceNames.add(interfaceName);
                        }
                    }
                }
            } else {
                serviceNames.add(serviceName);
            }
            LOGGER.debug("{} interface is ={}", instance.getClass().getName(), serviceNames);

            for (String realServiceName : serviceNames) {
                conf.setGroup(serviceAnnotation.group());
                conf.setService(realServiceName);
                conf.setType(serviceAnnotation.type());
                conf.setTarget(instance);
                conf.setVersion(serviceAnnotation.version());
                if (!ZebraConstants.DEFAULT_SET.equals(ZebraRun.APP_SET)) {
                    conf.setSet(ZebraRun.APP_SET);
                } else {
                    conf.setSet(serviceAnnotation.set());
                }
            }
            rpcSeviceList.add(conf);
        }

        return rpcSeviceList;
    }

    private Collection<Object> getTypedBeansWithAnnotation(Class<? extends Annotation > annotationType) {
        return Stream.of(applicationContext.getBeanNamesForAnnotation(annotationType)).filter(name -> {
            BeanDefinition beanDefinition = applicationContext.getBeanFactory().getBeanDefinition(name);
            if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
                StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                return metadata.isAnnotated(annotationType.getName());
            }
            return null != applicationContext.getBeanFactory().findAnnotationOnBean(name, annotationType);
        }).map(name -> applicationContext.getBeanFactory().getBean(name)).collect(Collectors.toList());
    }

    private boolean isGrpcServer(Object instance) {
        return instance instanceof io.grpc.BindableService;
    }
}
