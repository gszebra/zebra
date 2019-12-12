package com.guosen.zebra.core.grpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.Message;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.exception.ProtobufException;
import com.guosen.zebra.core.exception.RpcErrorMsgConstant;
import com.guosen.zebra.core.exception.RpcFrameworkException;
import com.guosen.zebra.core.exception.RpcServiceException;
import com.guosen.zebra.core.grpc.client.stream.GrpcStreamClientCall;
import com.guosen.zebra.core.grpc.client.unary.GrpcBlockingUnaryCommand;
import com.guosen.zebra.core.grpc.client.unary.GrpcFutureUnaryCommand;
import com.guosen.zebra.core.grpc.client.unary.GrpcHystrixCommand;
import com.guosen.zebra.core.grpc.client.unary.GrpcUnaryClientCall;
import com.guosen.zebra.core.grpc.stream.PoJo2ProtoStreamObserver;
import com.guosen.zebra.core.grpc.stream.Proto2PoJoStreamObserver;
import com.guosen.zebra.core.grpc.util.SerializerUtil;
import com.guosen.zebra.core.serializer.utils.ReflectUtils;

import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.stub.StreamObserver;

public abstract class AbstractClientInvocation implements InvocationHandler {

	protected abstract GrpcRequest buildGrpcRequest(Method method, Object[] args);

	public AbstractClientInvocation() {
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (ReflectUtils.isToStringMethod(method)) {
			return AbstractClientInvocation.this.toString();
		} else {
			GrpcRequest request = this.buildGrpcRequest(method, args);
			MethodType methodType = request.getMethodType();
			switch (methodType) {
			case UNARY:
				return unaryCall(request);
			case CLIENT_STREAMING:
				return streamCall(request);
			case SERVER_STREAMING:
				return streamCall(request);
			case BIDI_STREAMING:
				return streamCall(request);
			default:
				RpcServiceException rpcFramwork = new RpcServiceException(RpcErrorMsgConstant.SERVICE_UNFOUND);
				throw rpcFramwork;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Object streamCall(GrpcRequest request) {
		Map<String, Object> refUrl = request.getRefUrl();
		MethodType methodType = request.getMethodType();
		Class<?> returnType = request.getResponseType();
		MethodDescriptor<Message, Message> methodDesc = request.getMethodDescriptor();
		Object requestParam = request.getRequestParam();
		StreamObserver<Message> requestObserver;
		GrpcStreamClientCall clientCall = GrpcStreamClientCall.create(request, refUrl);
		switch (methodType) {
			case CLIENT_STREAMING:
				requestObserver = clientCall.asyncClientStream(methodDesc,
						Proto2PoJoStreamObserver.newObserverWrap((StreamObserver<Object>) requestParam, returnType));
				return PoJo2ProtoStreamObserver.newObserverWrap(requestObserver);
			case SERVER_STREAMING:
				Object responseObserver = request.getResponseOberver();
				try {
					Message messageParam = SerializerUtil.pojo2Protobuf(requestParam);
					clientCall.asyncServerStream(methodDesc,
							Proto2PoJoStreamObserver.newObserverWrap((StreamObserver<Object>) responseObserver, returnType),
							messageParam);
				} catch (ProtobufException e) {
					RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
					throw rpcFramwork;
				}
				return null;
			case BIDI_STREAMING:
				requestObserver = clientCall.asyncBidiStream(methodDesc,
						Proto2PoJoStreamObserver.newObserverWrap((StreamObserver<Object>) requestParam, returnType));
				return PoJo2ProtoStreamObserver.newObserverWrap(requestObserver);
			default:
				RpcServiceException rpcFramwork = new RpcServiceException(RpcErrorMsgConstant.SERVICE_UNFOUND);
				throw rpcFramwork;
		}
	}

	private Object unaryCall(GrpcRequest request) throws Exception {
		String serviceName = request.getServiceName();
		String methodName = request.getMethodName();
		Map<String, Object> refUrl = request.getRefUrl();
		Integer retryOption = this.buildRetryOption(methodName, refUrl);
		GrpcUnaryClientCall clientCall = GrpcUnaryClientCall.create(request, retryOption, refUrl);
		GrpcHystrixCommand hystrixCommand = null;
		Boolean isEnableFallback = this.buildFallbackOption(methodName, refUrl);
		switch (request.getCallType()) {
		case ZebraConstants.RPCTYPE_ASYNC:
			hystrixCommand = new GrpcFutureUnaryCommand(serviceName, methodName, isEnableFallback);
			break;
		case ZebraConstants.RPCTYPE_BLOCKING:
			hystrixCommand = new GrpcBlockingUnaryCommand(serviceName, methodName, isEnableFallback);
			break;
		default:
			hystrixCommand = new GrpcFutureUnaryCommand(serviceName, methodName, isEnableFallback);
			break;
		}
		hystrixCommand.setClientCall(clientCall);
		hystrixCommand.setRequest(request);
		return hystrixCommand.execute();
	}

	private Boolean buildFallbackOption(String methodName, Map<String, Object> refUrl) {
		Boolean isEnableFallback = (Boolean)refUrl.get(ZebraConstants.GRPC_FALLBACK_KEY);
		String[] methodNames = StringUtils.split(Arrays.toString((String [])refUrl.get(ZebraConstants.FALLBACK_METHODS_KEY)), ",");
		if (methodNames != null && methodNames.length > 0) {
			return isEnableFallback && Arrays.asList(methodNames).contains(methodName);
		} else {
			return isEnableFallback;
		}
	}

	private Integer buildRetryOption(String methodName, Map<String, Object> refUrl) {
		Integer retries = (Integer)refUrl.get(ZebraConstants.METHOD_RETRY_KEY);
		try{
			String[] methodNames = StringUtils.split(Arrays.toString((String [])refUrl.get(ZebraConstants.RETRY_METHODS_KEY)), ",");
			if (methodNames != null && methodNames.length > 0) {
				if (Arrays.asList(methodNames).contains(methodName)) {
					return retries;
				} else {
					return Integer.valueOf(0);
				}
			} else {
				return retries;
			}
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

}
