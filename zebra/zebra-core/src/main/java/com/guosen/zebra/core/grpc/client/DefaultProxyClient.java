package com.guosen.zebra.core.grpc.client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.serializer.utils.ClassHelper;
import com.guosen.zebra.core.serializer.utils.ReflectUtils;

public class DefaultProxyClient<T> implements GrpcProtocolClient<T> {

	private final Class<?> interfaceClass;

	private final Map<String, Object> refUrl;

	public DefaultProxyClient(Map<String, Object> refUrl) {
		this.refUrl = refUrl;
		String serviceName = (String) refUrl.get(ZebraConstants.SERVICE_NAME);
		try {
			this.interfaceClass = ReflectUtils.name2class(serviceName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getGrpcClient(GrpcProtocolClient.ChannelCall channelPoll, int callType, int callTimeout) {
		return (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { interfaceClass },
				new DefaultProxyClientInvocation(channelPoll, callType, callTimeout));
	}

	private class DefaultProxyClientInvocation extends AbstractClientInvocation {

		private final GrpcProtocolClient.ChannelCall channelPool;
		private int callType;
		private final int callTimeout;

		public DefaultProxyClientInvocation(GrpcProtocolClient.ChannelCall call, int callType, int callTimeout) {
			this.channelPool = call;
			this.callTimeout = callTimeout;
		}

		@Override
		protected GrpcRequest buildGrpcRequest(Method method, Object[] args) {
			boolean isLegalMethod = ReflectUtils.isLegal(method);
			if (isLegalMethod) {
				throw new IllegalArgumentException("remote call type do not support this method " + method.getName());
			}
			callType = ((boolean) refUrl.get(ZebraConstants.ASYNC_KEY)) ? 1 : 2;
			GrpcRequest request = new GrpcRequest.Default(DefaultProxyClient.this.refUrl, channelPool,
					(String) refUrl.get(ZebraConstants.SERVICE_NAME), method.getName(), args, callType, callTimeout);
			return request;
		}

	}

}
