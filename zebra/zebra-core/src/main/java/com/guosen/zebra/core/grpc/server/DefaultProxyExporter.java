package com.guosen.zebra.core.grpc.server;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.Message;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.exception.RpcErrorMsgConstant;
import com.guosen.zebra.core.exception.RpcServiceException;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;
import com.guosen.zebra.core.grpc.anotation.GrpcMethodType;
import com.guosen.zebra.core.grpc.util.GrpcUtil;
import com.guosen.zebra.core.serializer.utils.ReflectUtils;

import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.stub.ServerCalls;

public class DefaultProxyExporter implements GrpcProtocolExporter {
	private static final Logger log = LogManager.getLogger(DefaultProxyExporter.class);
	private final RpcServiceBaseInfo seviceConf;

	public DefaultProxyExporter(RpcServiceBaseInfo seviceConf) {
		this.seviceConf = seviceConf;
	}

	@Override
	public ServerServiceDefinition export(Class<?> protocol, Object protocolImpl) {
		Class<?> serivce = protocol;
		Object serviceRef = protocolImpl;
		String serviceName = seviceConf.getService();
		ServerServiceDefinition.Builder serviceDefBuilder = ServerServiceDefinition.builder(serviceName);
		log.debug("export ServerServiceDefinition,serivce = {}",serviceName);
		List<Method> methods = ReflectUtils.findAllPublicMethods(serivce);
		if (methods.isEmpty()||ZebraConstants.TYPE_GATEWATY.equals(seviceConf.getType())) {
			ServiceDescriptor.Builder build = ServiceDescriptor.newBuilder(serivce.getName());
			ServerServiceDefinition.Builder serviceBuilder = ServerServiceDefinition
					.builder(build.build());
			return serviceBuilder.build();
		}
		for (Method method : methods) {
			MethodDescriptor<Message, Message> methodDescriptor = GrpcUtil.createMethodDescriptor(serviceName, method);
			GrpcMethodType grpcMethodType = method.getAnnotation(GrpcMethodType.class);
			switch (grpcMethodType.methodType()) {
			case UNARY:
				serviceDefBuilder.addMethod(methodDescriptor,
						ServerCalls.asyncUnaryCall(new ServerInvocation(serviceRef, method, grpcMethodType, seviceConf)));
				break;
			case CLIENT_STREAMING:
				serviceDefBuilder.addMethod(methodDescriptor,
						ServerCalls.asyncClientStreamingCall(new ServerInvocation(serviceRef, method, grpcMethodType, seviceConf)));
				break;
			case SERVER_STREAMING:
				serviceDefBuilder.addMethod(methodDescriptor,
						ServerCalls.asyncServerStreamingCall(new ServerInvocation(serviceRef, method, grpcMethodType, seviceConf)));
				break;
			case BIDI_STREAMING:
				serviceDefBuilder.addMethod(methodDescriptor,
						ServerCalls.asyncBidiStreamingCall(new ServerInvocation(serviceRef, method, grpcMethodType, seviceConf)));
				break;
			default:
				RpcServiceException rpcFramwork = new RpcServiceException(RpcErrorMsgConstant.SERVICE_UNFOUND);
				throw rpcFramwork;
			}
		}
		log.info("'{}' service has been registered.", serviceName);
		return serviceDefBuilder.build();
	}

}
