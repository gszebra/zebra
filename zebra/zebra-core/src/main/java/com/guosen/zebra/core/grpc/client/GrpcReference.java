/**   
* @Title: GrpcReference.java 
* @Package com.guosen.zebra.core.grpc.client 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年11月2日 上午11:16:00 
* @version V1.0   
*/
package com.guosen.zebra.core.grpc.client;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.guosen.zebra.ZebraRun;
import com.guosen.zebra.core.boot.autoconfigure.GrpcProperties;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.anotation.ZebraReference;
import com.guosen.zebra.core.grpc.server.GenericService;
import com.guosen.zebra.core.server.RpcServiceExport;

import io.grpc.stub.AbstractStub;

/**
 * @ClassName: GrpcReference
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2017年11月2日 上午11:16:00
 * 
 */
public class GrpcReference extends RpcServiceExport{
	private GrpcProperties grpcProperties;
	public synchronized Object getProxyObj(ZebraReference reference, Class<?> referenceClass) {
		Object ref = null;
		try {
			Map<String, Object> params = Maps.newHashMap();
			if (isGenericClient(referenceClass)) {
				params.put(ZebraConstants.GENERIC_KEY, Boolean.TRUE);
			}else{
				params.put(ZebraConstants.GENERIC_KEY, Boolean.FALSE);
			}
			if (isGrpcStubClient(referenceClass)) {
				params.put(ZebraConstants.GRPC_STUB_KEY, Boolean.TRUE);
			}else{
				params.put(ZebraConstants.GRPC_STUB_KEY, Boolean.FALSE);
			}
			if (reference.fallback()) {
				params.put(ZebraConstants.GRPC_FALLBACK_KEY, Boolean.TRUE);
			}else{
				params.put(ZebraConstants.GRPC_FALLBACK_KEY, Boolean.FALSE);
			}
			String serviceName = getServiceName(reference,referenceClass);
//			params.put(ZebraConstants.CUST_NAME, custName);
			params.put(ZebraConstants.SERVICE_NAME, serviceName);
			params.put(ZebraConstants.GROUP_KEY,reference.group());
			params.put(ZebraConstants.VERSION_KEY,reference.version());
			params.put(ZebraConstants.APPLICATION_NAME,grpcProperties.getApplication());
			params.put(ZebraConstants.TIMEOUT, reference.timeOut());
			params.put(ZebraConstants.RETRY_METHODS_KEY, reference.retryMethods());
			params.put(ZebraConstants.FALLBACK_METHODS_KEY, reference.fallBackMethods());
			params.put(ZebraConstants.METHOD_RETRY_KEY, reference.retries());
			params.put(ZebraConstants.INTERFACECLASS_KEY, referenceClass);
			params.put(ZebraConstants.ASYNC_KEY, reference.async());
			params.put(ZebraConstants.ROUTE_KEY, reference.route());
			if(!"0".equals(reference.set())&&"0".equals(ZebraRun.APP_SET)){
				params.put(ZebraConstants.KEY_SET, reference.set());
			}else{
				params.put(ZebraConstants.KEY_SET, ZebraRun.APP_SET);
			}
			params.put(ZebraConstants.KEY_CLIENT_INTERCEPTOR,reference.interceptors());
			super.setGrpcProperties(grpcProperties);
			ref = super.getGrpcEngine().getClient(params);
		} catch (Exception e) {
			throw new IllegalStateException("Create Grpc client failed!", e);
		}
		return ref;
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

	public GrpcProperties getGrpcProperties() {
		return grpcProperties;
	}

	public void setGrpcProperties(GrpcProperties grpcProperties) {
		this.grpcProperties = grpcProperties;
	}
}
