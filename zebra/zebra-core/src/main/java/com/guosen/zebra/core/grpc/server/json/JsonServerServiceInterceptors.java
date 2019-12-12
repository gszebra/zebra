package com.guosen.zebra.core.grpc.server.json;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.util.GrpcUtil;
import com.guosen.zebra.core.message.json.JsonRequest;
import com.guosen.zebra.core.serializer.utils.JReflectionUtils;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;

/**
 * @ClassName: JsonServerServiceDefinition
 * @Description: JSON泛化调用支持
 * @author 邓启翔
 * @date 2017年11月17日 下午5:59:11
 * 
 */
public class JsonServerServiceInterceptors {
	public static Map<String,Class<?>> reqClzs = Maps.newConcurrentMap();
	
	public static ServerServiceDefinition useJsonMessages(final ServerServiceDefinition serviceDef)
			throws IllegalArgumentException, IllegalAccessException {
		return useMarshalledMessages(serviceDef,
				io.grpc.protobuf.ProtoUtils.marshaller(GrpcUtil.createDefaultInstance(JsonRequest.class)));
	}

	public static <T> ServerServiceDefinition useMarshalledMessages(final ServerServiceDefinition serviceDef,
			final MethodDescriptor.Marshaller<T> marshaller) throws IllegalArgumentException, IllegalAccessException {
		List<ServerMethodDefinition<?, ?>> wrappedMethods = new ArrayList<ServerMethodDefinition<?, ?>>();
		List<MethodDescriptor<?, ?>> wrappedDescriptors = new ArrayList<MethodDescriptor<?, ?>>();
		// Wrap the descriptors
		for (final ServerMethodDefinition<?, ?> definition : serviceDef.getMethods()) {
			List<Field> fields = new ArrayList<>();
			fields = JReflectionUtils.getAllFields(fields, definition.getMethodDescriptor().getRequestMarshaller().getClass());
			for (Field f : fields) {
				if("defaultInstance".equals(f.getName())){
					f.setAccessible(true);
					try {
						String fullMethodName =definition.getMethodDescriptor().getFullMethodName();
						fullMethodName = fullMethodName.split("/")[0] +ZebraConstants.ZEBRA_JSON_PREFIX+"/"+fullMethodName.split("/")[1];
						reqClzs.put(fullMethodName, f.get(definition.getMethodDescriptor().getRequestMarshaller()).getClass());
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			final MethodDescriptor<?, ?> originalMethodDescriptor = definition.getMethodDescriptor();
			final MethodDescriptor<T, T> wrappedMethodDescriptor = originalMethodDescriptor
					.toBuilder(marshaller, marshaller).build();
			wrappedDescriptors.add(wrappedMethodDescriptor);
			wrappedMethods.add(wrapMethod(definition, wrappedMethodDescriptor));
		}
		// Build the new service descriptor
		ServiceDescriptor.Builder build = ServiceDescriptor.newBuilder(serviceDef.getServiceDescriptor().getName()+ZebraConstants.ZEBRA_JSON_PREFIX);
		for(MethodDescriptor<?, ?> md :wrappedDescriptors){
			List<Field> fields  = Lists.newArrayList();
			JReflectionUtils.getAllFields(fields, md.getClass());
			for(Field f:fields){
				if(f.getName().equals("fullMethodName")){
					f.setAccessible(true);
					String fullMethodName = (String) f.get(md);
					fullMethodName = fullMethodName.split("/")[0] +ZebraConstants.ZEBRA_JSON_PREFIX+"/"+fullMethodName.split("/")[1];
					f.set(md, fullMethodName);
				}
			}
			build.addMethod(md);
		}
		final ServerServiceDefinition.Builder serviceBuilder = ServerServiceDefinition
				.builder(build.build());
		// Create the new service definiton.
		for (ServerMethodDefinition<?, ?> definition : wrappedMethods) {
//			MethodDescriptor<?, ?> methodDesc = definition.getMethodDescriptor();
//			List<Field> fields  = Lists.newArrayList();
//			JReflectionUtils.getAllFields(fields, methodDesc.getClass());
//			for(Field f:fields){
//				if(f.getName().equals("fullMethodName")){
//					f.setAccessible(true);
//					String fullMethodName = (String) f.get(methodDesc);
//					fullMethodName = fullMethodName +"json";
//					f.set(methodDesc, fullMethodName);
//				}
//			}
			serviceBuilder.addMethod(definition);
		}
		return serviceBuilder.build();
	}

	@SuppressWarnings("unused")
	private static <ReqT, RespT> void wrapAndAddMethod(ServerServiceDefinition.Builder serviceDefBuilder,
			ServerMethodDefinition<ReqT, RespT> method, List<? extends ServerInterceptor> interceptors) {
		ServerCallHandler<ReqT, RespT> callHandler = method.getServerCallHandler();
		for (ServerInterceptor interceptor : interceptors) {
			callHandler = InterceptCallHandler.create(interceptor, callHandler);
		}
		serviceDefBuilder.addMethod(method.withServerCallHandler(callHandler));
	}

	static final class InterceptCallHandler<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
		public static <ReqT, RespT> InterceptCallHandler<ReqT, RespT> create(ServerInterceptor interceptor,
				ServerCallHandler<ReqT, RespT> callHandler) {
			return new InterceptCallHandler<ReqT, RespT>(interceptor, callHandler);
		}

		private final ServerInterceptor interceptor;
		private final ServerCallHandler<ReqT, RespT> callHandler;

		private InterceptCallHandler(ServerInterceptor interceptor, ServerCallHandler<ReqT, RespT> callHandler) {
			this.interceptor = Preconditions.checkNotNull(interceptor, "interceptor");
			this.callHandler = callHandler;
		}

		@Override
		public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> call, Metadata headers) {
			return interceptor.interceptCall(call, headers, callHandler);
		}
	}

	private static <OReqT, ORespT, WReqT, WRespT> ServerMethodDefinition<WReqT, WRespT> wrapMethod(
			final ServerMethodDefinition<OReqT, ORespT> definition,
			final MethodDescriptor<WReqT, WRespT> wrappedMethod) {
		final ServerCallHandler<WReqT, WRespT> wrappedHandler = wrapHandler(definition.getServerCallHandler(),
				definition.getMethodDescriptor(), wrappedMethod);
		return ServerMethodDefinition.create(wrappedMethod, wrappedHandler);
	}
	@SuppressWarnings("unchecked")
	private static <OReqT, ORespT, WReqT, WRespT> ServerCallHandler<WReqT, WRespT> wrapHandler(
			final ServerCallHandler<OReqT, ORespT> originalHandler,
			final MethodDescriptor<OReqT, ORespT> originalMethod, final MethodDescriptor<WReqT, WRespT> wrappedMethod) {
		return new ServerCallHandler<WReqT, WRespT>() {
			@SuppressWarnings("rawtypes")
			@Override
			public ServerCall.Listener<WReqT> startCall(final ServerCall<WReqT, WRespT> call, final Metadata headers) {
				final ServerCall<OReqT, ORespT> unwrappedCall = new ZebraJsonForwardingServerCall<OReqT, ORespT>((ServerCall<ORespT, ORespT>) call);
				final ServerCall.Listener<OReqT> originalListener = originalHandler.startCall(unwrappedCall, headers);
				return new ServerJsonListener(originalListener,unwrappedCall);
			}
		};
	}
}
