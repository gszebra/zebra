package com.guosen.zebra.core.grpc.client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.server.GenericService;
import com.guosen.zebra.core.serializer.utils.ClassHelper;

public class GenericProxyClient<T> implements GrpcProtocolClient<T> {

	private final Map<String, Object> params;

	public GenericProxyClient(Map<String, Object> params) {
		this.params = params;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getGrpcClient(GrpcProtocolClient.ChannelCall channelPool, int callType, int callTimeout) {
		return (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { GenericService.class },
				new GenericProxyClientInvocation(channelPool, callType, callTimeout));
	}

	private class GenericProxyClientInvocation extends AbstractClientInvocation {

		private final GrpcProtocolClient.ChannelCall channelPool;
		private final int callType;
		private final int callTimeout;

		public GenericProxyClientInvocation(GrpcProtocolClient.ChannelCall channelPool, int callType, int callTimeout) {
			this.channelPool = channelPool;
			this.callType = callType;
			this.callTimeout = callTimeout;
		}

		@Override
		protected GrpcRequest buildGrpcRequest(Method method, Object[] args) {
			Map<String, Object> resetRefUrl = params;
			resetRefUrl.put(ZebraConstants.GROUP_KEY, getGroup(args));
			resetRefUrl.put(ZebraConstants.VERSION_KEY, getVersion(args));
			Object[] params = new Object[1];
			if (args.length > 5) {
				resetRefUrl.put(ZebraConstants.KEY_SET, getSet(args));
			}
			if (args.length > 6) {
				resetRefUrl.put(ZebraConstants.KEY_ADDR, getAddr(args));
			}
			params[0] = this.getArg(args);
			GrpcRequest request = new GrpcRequest.Default(resetRefUrl, channelPool, getServiceName(args),this.getMethod(args), params,
					callType, callTimeout);
			return request;
		}

		private String getServiceName(Object[] args) {
			return (String) args[0];
		}

		private String getGroup(Object[] args) {
			return (String) args[1];
		}

		private String getVersion(Object[] args) {
			return (String) args[2];
		}

		private String getMethod(Object[] args) {
			return (String) args[args.length - 2];
		}

		private String getSet(Object[] args) {
			return (String) args[3];
		}
		
		private String getAddr(Object[] args) {
			if((String) args[4] ==null) return "";
			return (String) args[4];
		}

		private Object getArg(Object[] args) {
			Object param = (Object) args[args.length - 1];
			return param;
		}

	}
}
