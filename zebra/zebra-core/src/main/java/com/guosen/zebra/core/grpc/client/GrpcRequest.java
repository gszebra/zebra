package com.guosen.zebra.core.grpc.client;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.exception.RpcFrameworkException;
import com.guosen.zebra.core.grpc.anotation.GrpcMethodType;
import com.guosen.zebra.core.grpc.util.GrpcUtil;
import com.guosen.zebra.core.serializer.utils.ReflectUtils;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;

public interface GrpcRequest {

	public Class<?> getResponseType();

	public MethodDescriptor<Message, Message> getMethodDescriptor();

	public Channel getChannel();

	public void returnChannel(Channel channel);

	public String getServiceName();

	public String getMethodName();

	public Object getRequestParam();

	public Object getResponseOberver();

	public Map<String, Object> getRefUrl();

	public int getCallType();

	public int getCallTimeout();

	public io.grpc.MethodDescriptor.MethodType getMethodType();

	public boolean getIsJson();
	
	public boolean getIsInputstream();

	public MethodDescriptor<Message, Message> getJsonMethodDesc();

	public MethodDescriptor<InputStream, InputStream> getIsMethodDesc();
	
	public MethodDescriptor<Message, Message> getHealthMethodDesc();

	public GrpcMethodType getGrpcMethodType();

	public class Default implements GrpcRequest, Serializable {
		
		private static final long serialVersionUID = 1L;

		private final Map<String, Object> refUrl;

		private final GrpcProtocolClient.ChannelCall chanelPool;

		private final String methodName;
		
		private final String serverName;

		private final Object[] args;

		private final int callType;

		private final int callTimeout;

		private GrpcMethodType grpcMethodType;

		public boolean isJson = false;
		
		public boolean isInputStream = false;
		
		public Default(Map<String, Object> refUrl, GrpcProtocolClient.ChannelCall chanelPool,String serverName, String methodName,
				Object[] args, int callType, int callTimeout) {
			super();
			this.refUrl = refUrl;
			this.serverName = serverName;
			this.chanelPool = chanelPool;
			this.methodName = methodName;
			if (args.length > 2) {
				throw new IllegalArgumentException(
						"grpc not support multiple args,args is " + args + " length is " + args.length);
			} else {
				this.args = args;
				if (this.args[0] instanceof JSONObject) {
					isJson = true;
				}
				if (this.args[0] instanceof InputStream) {
					isInputStream = true;
				}
			}
			this.callType = callType;
			this.callTimeout = callTimeout;
			try {
				if (!isJson && !isInputStream) {
					Class<?> service = ReflectUtils.forName(this.getServiceName());
					Method method = ReflectUtils.findMethodByMethodName(service, this.getMethodName());
					grpcMethodType = method.getAnnotation(GrpcMethodType.class);
				}
			} catch (Exception e) {
				RpcFrameworkException framworkException = new RpcFrameworkException(e);
				throw framworkException;
			}
		}

		@Override
		public Object getRequestParam() {
			return args[0];
		}

		@Override
		public MethodDescriptor<Message, Message> getMethodDescriptor() {
			return GrpcUtil.createMethodDescriptor(this.getServiceName(), methodName, grpcMethodType);
		}

		@Override
		public Class<?> getResponseType() {
			return grpcMethodType.responseType();
		}

		@Override
		public Channel getChannel() {
			return chanelPool.borrowChannel(refUrl,serverName);
		}

		@Override
		public void returnChannel(Channel channel) {
			chanelPool.returnChannel(refUrl,serverName, channel);
		}

		@Override
		public String getServiceName() {
			return serverName;
		}

		@Override
		public Map<String, Object> getRefUrl() {
			this.refUrl.put(ZebraConstants.METHOD_KEY, methodName);//
			if(!getIsInputstream()){
				this.refUrl.put(ZebraConstants.ARG_KEY, new Gson().toJson(getRequestParam()));
			}
			return refUrl;
		}

		@Override
		public String getMethodName() {
			return this.methodName;
		}

		@Override
		public int getCallType() {
			return this.callType;
		}

		@Override
		public int getCallTimeout() {
			return this.callTimeout;
		}

		@Override
		public io.grpc.MethodDescriptor.MethodType getMethodType() {
			if(isInputStream||isJson){
				return MethodType.UNARY;
			}
			return this.grpcMethodType.methodType();
		}

		@Override
		public Object getResponseOberver() {
			return args[1];
		}

		@Override
		public boolean getIsJson() {
			return isJson;
		}

		@Override
		public boolean getIsInputstream() {
			return isInputStream;
		}

		@Override
		public MethodDescriptor<Message, Message> getJsonMethodDesc() {
			return GrpcUtil.createJsonMethodDescriptor(this.getServiceName(), methodName);
		}

		@Override
		public MethodDescriptor<InputStream, InputStream> getIsMethodDesc() {
			return GrpcUtil.createInputStreamMethodDescriptor(this.getServiceName(), methodName);
		}

		@Override
		public GrpcMethodType getGrpcMethodType() {
			return grpcMethodType;
		}


		@Override
		public MethodDescriptor<Message, Message> getHealthMethodDesc() {
			// TODO Auto-generated method stub
			return GrpcUtil.createHealthMethodDescriptor(this.getServiceName(), methodName);
		}
	}
}
