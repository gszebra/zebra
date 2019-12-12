package com.guosen.zebra.core.grpc.server;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;
import com.guosen.zebra.core.grpc.anotation.GrpcMethodType;
import com.guosen.zebra.core.grpc.stream.PoJo2ProtoStreamObserver;
import com.guosen.zebra.core.grpc.stream.Proto2PoJoStreamObserver;
import com.guosen.zebra.core.grpc.util.PropertiesContent;
import com.guosen.zebra.core.grpc.util.SerializerUtil;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCalls.BidiStreamingMethod;
import io.grpc.stub.ServerCalls.ClientStreamingMethod;
import io.grpc.stub.ServerCalls.ServerStreamingMethod;
import io.grpc.stub.StreamObserver;
import io.netty.util.internal.ThrowableUtil;

public class ServerInvocation
		implements io.grpc.stub.ServerCalls.UnaryMethod<Message, Message>, ServerStreamingMethod<Message, Message>,
		ClientStreamingMethod<Message, Message>, BidiStreamingMethod<Message, Message> {
	private static final Logger log = LogManager.getLogger(ServerInvocation.class);
	private static final Logger busigger = LogManager.getLogger("biz");

	private final Object serviceToInvoke;

	private final Method method;

	private final RpcServiceBaseInfo seviceConf;

	private final GrpcMethodType grpcMethodType;

	private static boolean enableRetLog = true;

	public ServerInvocation(Object serviceToInvoke, Method method, GrpcMethodType grpcMethodType,
			RpcServiceBaseInfo seviceConf) {
		this.serviceToInvoke = serviceToInvoke;
		this.method = method;
		this.grpcMethodType = grpcMethodType;
		this.seviceConf = seviceConf;
		try {
			enableRetLog = PropertiesContent.getbooleanValue(ZebraConstants.ZEBRA_ENABLE_RET_LOG, true);
		} catch (Exception e) {
			enableRetLog = true;
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public StreamObserver<Message> invoke(StreamObserver<Message> responseObserver) {
		try {
			Class<?> requestType = grpcMethodType.requestType();
			PoJo2ProtoStreamObserver servserResponseObserver = PoJo2ProtoStreamObserver
					.newObserverWrap(responseObserver);
			Object result = method.invoke(serviceToInvoke, servserResponseObserver);
			return Proto2PoJoStreamObserver.newObserverWrap((StreamObserver<Object>) result, requestType);
		} catch (Throwable e) {
			String stackTrace = ThrowableUtil.stackTraceToString(e);
			log.error(e.getMessage(), e);
			StatusRuntimeException statusException = Status.UNAVAILABLE.withDescription(stackTrace)
					.asRuntimeException();
			responseObserver.onError(statusException);
		}
		return null;
	}

	@Override
	public void invoke(Message request, StreamObserver<Message> responseObserver) {
		switch (grpcMethodType.methodType()) {
		case UNARY:
			unaryCall(request, responseObserver);
			break;
		case SERVER_STREAMING:
			streamCall(request, responseObserver);
			break;
		default:
			break;
		}
	}

	private void streamCall(Message request, StreamObserver<Message> responseObserver) {
		try {
			busigger.info(
					"{\"time\":\"{}\",\"service\":\"{}\",\"method\":\"{}\",\"rpcContext\":{},\"attachments\":{},\"req\":{}}",
					DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"), EtcdRegistry.getServiceName(),
					method.getName(), JSON.toJSONString(RpcContext.getContext().get()),
					JSON.toJSONString(RpcContext.getContext().getAttachments()),
					JsonFormat.printer().omittingInsignificantWhitespace().print(request));
			Class<?> requestType = grpcMethodType.requestType();
			Object reqPojo = SerializerUtil.protobuf2Pojo(request, requestType);
			Object[] requestParams = new Object[] { reqPojo,
					PoJo2ProtoStreamObserver.newObserverWrap(responseObserver) };
			method.invoke(serviceToInvoke, requestParams);
		} catch (Throwable e) {
			String stackTrace = ThrowableUtil.stackTraceToString(e);
			log.error(e.getMessage(), e);
			StatusRuntimeException statusException = Status.UNAVAILABLE.withDescription(stackTrace)
					.asRuntimeException();
			responseObserver.onError(statusException);
		}
	}

	private void unaryCall(Message request, StreamObserver<Message> responseObserver) {
		Message reqProtoBufer = request;
		try {
			busigger.info(
					"{\"time\":\"{}\",\"service\":\"{}\",\"method\":\"{}\",\"rpcContext\":{},\"attachments\":{},\"req\":{}}",
					DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"), EtcdRegistry.getServiceName(),
					method.getName(), JSON.toJSONString(RpcContext.getContext().get()),
					JSON.toJSONString(RpcContext.getContext().getAttachments()),
					JsonFormat.printer().omittingInsignificantWhitespace().print(request));
			Class<?> requestType = grpcMethodType.requestType();
			Object reqPojo = SerializerUtil.protobuf2Pojo(reqProtoBufer, requestType);
			Object[] requestParams = new Object[] { reqPojo };
			Object respPojo = method.invoke(serviceToInvoke, requestParams);
			if (respPojo == null && RpcContext.getContext().getCfResult() != null) {
				Map<String, String> attachments = RpcContext.getContext().getAttachments();
				Map<String, Object> values = RpcContext.getContext().get();
				RpcContext.getContext().getCfResult().whenComplete((v, t) -> {
					if (t != null) {
						String stackTrace = ThrowableUtil.stackTraceToString(t);
						StatusRuntimeException statusException = Status.UNAVAILABLE.withDescription(stackTrace)
								.asRuntimeException();
						responseObserver.onError(statusException);
					} else {
						try {
							RpcContext.getContext().get().putAll(values);
							RpcContext.getContext().getAttachments().putAll(attachments);
							Message respProtoBufer = SerializerUtil.pojo2Protobuf(v);
							if (enableRetLog) {
								busigger.info(
										"{\"time\":\"{}\",\"service\":\"{}\",\"method\":\"{}\",\"rpcContext\":{},\"attachments\":{},\"rsp\":{}}",
										DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"),
										EtcdRegistry.getServiceName(), method.getName(), JSON.toJSONString(values),
										JSON.toJSONString(attachments),
										JsonFormat.printer().omittingInsignificantWhitespace().print(respProtoBufer));
							}
							responseObserver.onNext(respProtoBufer);
							responseObserver.onCompleted();
						} catch (Exception e) {
							String stackTrace = ThrowableUtil.stackTraceToString(e);
							StatusRuntimeException statusException = Status.UNAVAILABLE.withDescription(stackTrace)
									.asRuntimeException();
							responseObserver.onError(statusException);
						}
					}
				});
			} else {
				Message respProtoBufer = SerializerUtil.pojo2Protobuf(respPojo);
				if (enableRetLog) {
					busigger.info(
							"{\"time\":\"{}\",\"service\":\"{}\",\"method\":\"{}\",\"rpcContext\":{},\"attachments\":{},\"rsp\":{}}",
							DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"),
							EtcdRegistry.getServiceName(), method.getName(),
							JSON.toJSONString(RpcContext.getContext().get()),
							JSON.toJSONString(RpcContext.getContext().getAttachments()),
							JsonFormat.printer().omittingInsignificantWhitespace().print(respProtoBufer));
				}
				responseObserver.onNext(respProtoBufer);
				responseObserver.onCompleted();
			}
		} catch (Throwable e) {
			String stackTrace = ThrowableUtil.stackTraceToString(e);
			log.error(e.getMessage(), e);
			StatusRuntimeException statusException = Status.UNAVAILABLE.withDescription(stackTrace)
					.asRuntimeException();
			responseObserver.onError(statusException);
		}
	}

	public String getRpcName() {
		return this.seviceConf.getService() + ":" + method.getName();
	}

}
