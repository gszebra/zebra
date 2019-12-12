package com.guosen.zebra.core.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ClassUtils;

/** 
* @ClassName: GrpcAop 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2017年10月31日 上午10:20:31 
*  
*/
public class GrpcAop {

  public static Object getTarget(Object proxy) throws Exception {
    if (!isAopProxy(proxy)) {
      return proxy;
    } else {
      if (AopUtils.isJdkDynamicProxy(proxy)) {
        return getJdkDynamicProxyTargetObject(proxy);
      } else {
        return getCglibProxyTargetObject(proxy);
      }
    }

  }

  private static boolean isAopProxy(Object object) {
    return Proxy.isProxyClass(object.getClass()) || ClassUtils.isCglibProxyClass(object.getClass());
  }

  private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
    String name = proxy.getClass().getName();
    if (name.toLowerCase().contains("cglib")) {
      Method[] methods = proxy.getClass().getDeclaredMethods();
      Method targetSourceMethod = null;
      for (Method method : methods) {
        if (method.getName().endsWith("getTargetSource")) {
          targetSourceMethod = method;
        }
      }
      if (targetSourceMethod != null) {
        targetSourceMethod.setAccessible(true);
        try {
          TargetSource targetSource = (TargetSource) targetSourceMethod.invoke(proxy);
          return targetSource.getTarget();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      } else {
        throw new IllegalStateException(
            "Could not find target source method on proxied object [" + proxy.getClass() + "]");
      }
    }
    return proxy;
  }

  private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
    Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
    h.setAccessible(true);
    AopProxy aopProxy = (AopProxy) h.get(proxy);
    Field advised = aopProxy.getClass().getDeclaredField("advised");
    advised.setAccessible(true);
    Object target = ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
    return target;
  }
}
