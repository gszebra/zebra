package com.guosen.zebra.core.grpc.client.unary;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.Message;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.exception.ProtobufException;
import com.guosen.zebra.core.exception.RpcFrameworkException;
import com.guosen.zebra.core.grpc.client.GrpcRequest;
import com.guosen.zebra.core.grpc.client.GrpcResponse;
import com.guosen.zebra.core.grpc.util.GrpcUtil;
import com.guosen.zebra.core.grpc.util.SerializerUtil;
import com.guosen.zebra.core.message.json.JsonRequest;

import io.grpc.MethodDescriptor;

/**
 * @ClassName: GrpcHystrixCommand
 * @Description: HystrixCommand 线程上下文切换过于频繁，性能太差，暂时不用
 * @author 邓启翔
 * @date 2018年6月8日 上午8:38:44
 * 
 */
public abstract class GrpcHystrixCommand {

	private static final Logger log = LogManager.getLogger(GrpcHystrixCommand.class);

	private static Cache<String, AtomicInteger> cache = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.SECONDS)
			.build();

	private GrpcRequest request;

	private GrpcUnaryClientCall clientCall;

	private final String serviceName;

	private final String methodName;

	private final boolean isEnabledFallBack;

	public GrpcHystrixCommand(String serviceName, String methodName, Boolean isEnabledFallBack) {
		this.serviceName = serviceName;
		this.methodName = methodName;
		this.isEnabledFallBack = isEnabledFallBack;
	}

	public void setRequest(GrpcRequest request) {
		this.request = request;
	}

	public void setClientCall(GrpcUnaryClientCall clientCall) {
		this.clientCall = clientCall;
	}

	public Object execute() throws Exception {
		try {
			if (!ZebraConstants.START_UP_FUSING && cache.getIfPresent(serviceName + methodName) != null
					&& cache.getIfPresent(serviceName + methodName).get() > 30) {
				if (isEnabledFallBack) {
					return getFallback();
				}
			}
			Integer timeOut = this.request.getCallTimeout();
			if (log.isDebugEnabled()) {
				MethodDescriptor<Message, Message> methodDesc = this.request.getJsonMethodDesc() == null
						? this.request.getMethodDescriptor() : this.request.getJsonMethodDesc();
				if (methodDesc != null) {
					log.debug("rpc service is {}, request is {},attachments is {}",methodDesc.getFullMethodName(), request.getRequestParam(),
							RpcContext.getContext().getAttachments());
				}
			}
			if (request.getIsJson()) {
				MethodDescriptor<Message, Message> methodDesc = this.request.getJsonMethodDesc();
				JsonRequest request = JsonRequest.newBuilder()
						.setMessage(((JSONObject) this.request.getRequestParam()).toJSONString()).build();
				return this.runJs(request, methodDesc, timeOut, clientCall);
			} else if (request.getIsInputstream()) {
				MethodDescriptor<InputStream, InputStream> methodDesc = this.request.getIsMethodDesc();
				InputStream request = (InputStream) this.request.getRequestParam();
				return this.runIs(request, methodDesc, timeOut, clientCall);
			} else {
				MethodDescriptor<Message, Message> methodDesc = this.request.getMethodDescriptor();
				Message request = getRequestMessage();
				if (this instanceof GrpcBlockingUnaryCommand) {
					Message response = (Message) this.run0(request, methodDesc, timeOut, clientCall,
							this.request.getResponseType());
					return this.transformMessage(response);
				} else {
					return this.run0(request, methodDesc, timeOut, clientCall, this.request.getResponseType());
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (!ZebraConstants.START_UP_FUSING && cache.getIfPresent(serviceName + methodName) == null) {
				cache.put(serviceName + methodName, new AtomicInteger(0));
			}
			cache.getIfPresent(serviceName + methodName).getAndIncrement();
			if (isEnabledFallBack) {
				return getFallback();
			}
			throw e;
		}
	}

	private Object getFallback() {
		Class<?> responseType = this.request.getResponseType();
		Message response = GrpcUtil.createDefaultInstance(responseType);
		Object obj = this.transformMessage(response);
		return obj;
	}

	private Message getRequestMessage() {
		try {
			Object param = this.request.getRequestParam();
			return SerializerUtil.pojo2Protobuf(param);
		} catch (ProtobufException e) {
			RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
			throw rpcFramwork;
		}
	}

	private Object transformMessage(Message message) {
		Class<?> respPojoType = request.getResponseType();
		GrpcResponse response = new GrpcResponse.Default(message, respPojoType);
		try {
			return response.getResponseArg();
		} catch (ProtobufException e) {
			RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
			throw rpcFramwork;
		}
	}

	protected abstract Message run0(Message req, MethodDescriptor<Message, Message> methodDesc, Integer timeOut,
			GrpcUnaryClientCall clientCall, Class<?> responseType);

	protected abstract InputStream runIs(InputStream req, MethodDescriptor<InputStream, InputStream> methodDesc,
			Integer timeOut, GrpcUnaryClientCall clientCall);

	protected abstract JSONObject runJs(Message req, MethodDescriptor<Message, Message> methodDesc, Integer timeOut,
			GrpcUnaryClientCall clientCall);
}
