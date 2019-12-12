/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.guosen.zebra.core.grpc.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.protobuf.Message;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.anotation.GrpcMethodType;
import com.guosen.zebra.core.message.json.JsonReply;
import com.guosen.zebra.core.message.json.JsonRequest;
import com.guosen.zebra.core.monitor.health.Health;
import com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest;
import com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse;
import com.guosen.zebra.core.serializer.ProtobufEntity;
import com.guosen.zebra.core.serializer.utils.ReflectUtils;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;

@SuppressWarnings("unchecked")
public class GrpcUtil {
	private static Map<String,Object> methodDescriptorCache = Maps.newHashMap();
	
	final static MethodDescriptor.Marshaller<InputStream> marshaller = new MethodDescriptor.Marshaller<InputStream>() {
		@Override
		public InputStream stream(final InputStream value) {
			return value;
		}

		@Override
		public InputStream parse(final InputStream stream) {
			if (stream.markSupported()) {
				return stream;
			} else {
				return new BufferedInputStream(stream);
			}
		}
	};

	private GrpcUtil() {
	}

	public static final Metadata.Key<String> GRPC_CONTEXT_ATTACHMENTS = Metadata.Key.of("grpc_header_attachments-bin",
			utf8Marshaller());

	public static final Metadata.Key<String> GRPC_CONTEXT_VALUES = Metadata.Key.of("grpc_header_values-bin",
			utf8Marshaller());

	private static Metadata.BinaryMarshaller<String> utf8Marshaller() {
		return new Metadata.BinaryMarshaller<String>() {

			@Override
			public byte[] toBytes(String value) {
				return value.getBytes(Charsets.UTF_8);
			}

			@Override
			public String parseBytes(byte[] serialized) {
				return new String(serialized, Charsets.UTF_8);
			}
		};
	}

	public static io.grpc.MethodDescriptor<Message, Message> createMethodDescriptor(String serverName, Method method) {
//		String clzzName = clzz.getName();
		String methodName = method.getName();
		GrpcMethodType grpcMethodType = method.getAnnotation(GrpcMethodType.class);
		Message argsReq = createDefaultInstance(grpcMethodType.requestType());
		Message argsRep = createDefaultInstance(grpcMethodType.responseType());
		return io.grpc.MethodDescriptor.<Message, Message> newBuilder().setType(grpcMethodType.methodType())//
				.setFullMethodName(io.grpc.MethodDescriptor.generateFullMethodName(serverName, methodName))//
				.setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsReq))//
				.setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsRep))//
				.setSafe(false)//
				.setIdempotent(false)//
				.build();
	}
	
	public static io.grpc.MethodDescriptor<Message, Message> createMethodDescriptor(String clzzName, String methodName,
			GrpcMethodType grpcMethodType) {
		io.grpc.MethodDescriptor<Message, Message> methodDescriptor = (MethodDescriptor<Message, Message>) methodDescriptorCache
				.get(clzzName + methodName);
		if(methodDescriptor!=null) return methodDescriptor;
		else{
			Message argsReq = createDefaultInstance(grpcMethodType.requestType());
			Message argsRep = createDefaultInstance(grpcMethodType.responseType());
			methodDescriptor = io.grpc.MethodDescriptor.<Message, Message> newBuilder().setType(grpcMethodType.methodType())//
					.setFullMethodName(io.grpc.MethodDescriptor.generateFullMethodName(clzzName, methodName))//
					.setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsReq))//
					.setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsRep))//
					.setSafe(false)//
					.setIdempotent(false)//
					.build();
			methodDescriptorCache.put(clzzName + methodName, methodDescriptor);
			return methodDescriptor;
		}
		

	}

	public static io.grpc.MethodDescriptor<Message, Message> createJsonMethodDescriptor(String clzzName,
			String methodName) {
		io.grpc.MethodDescriptor<Message, Message> methodDescriptor = (MethodDescriptor<Message, Message>) methodDescriptorCache
				.get(clzzName+ZebraConstants.ZEBRA_JSON_PREFIX + methodName);
		if(methodDescriptor!=null) return methodDescriptor;
		else{
			Message argsReq = createDefaultInstance(JsonRequest.class);
			Message argsRep = createDefaultInstance(JsonReply.class);
			methodDescriptor = io.grpc.MethodDescriptor.<Message, Message> newBuilder().setType(MethodType.UNARY)//
					.setFullMethodName(io.grpc.MethodDescriptor.generateFullMethodName(clzzName+ZebraConstants.ZEBRA_JSON_PREFIX, methodName))//
					.setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsReq))//
					.setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsRep))//
					.setSafe(false)//
					.setIdempotent(false)//
					.build();
			methodDescriptorCache.put(clzzName+ZebraConstants.ZEBRA_JSON_PREFIX + methodName, methodDescriptor);
			return methodDescriptor;
		}
	}

	public static io.grpc.MethodDescriptor<InputStream, InputStream> createInputStreamMethodDescriptor(String clzzName,
			String methodName) {
		io.grpc.MethodDescriptor<InputStream, InputStream> methodDescriptor = (MethodDescriptor<InputStream, InputStream>) methodDescriptorCache
				.get(clzzName +ZebraConstants.ZEBRA_IS_PREFIX + methodName);
		if (methodDescriptor != null)
			return methodDescriptor;
		else {
			methodDescriptor = io.grpc.MethodDescriptor.<InputStream, InputStream> newBuilder()
					.setType(MethodType.UNARY)//
					.setFullMethodName(io.grpc.MethodDescriptor.generateFullMethodName(clzzName, methodName))//
					.setRequestMarshaller(marshaller)//
					.setResponseMarshaller(marshaller)//
					.setSafe(false)//
					.setIdempotent(false)//
					.build();
			methodDescriptorCache.put(clzzName +ZebraConstants.ZEBRA_IS_PREFIX + methodName, methodDescriptor);
			return methodDescriptor;
		}
	}
	public static io.grpc.MethodDescriptor<Message, Message> createHealthMethodDescriptor(String clzzName,
			String methodName) {
		Message argsReq = createDefaultInstance(HealthRequest.class);
		Message argsRep = createDefaultInstance(HealthResponse.class);
		return io.grpc.MethodDescriptor.<Message, Message> newBuilder().setType(MethodType.UNARY)//
				.setFullMethodName(io.grpc.MethodDescriptor.generateFullMethodName(Health.class.getName(), methodName))//
				.setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsReq))//
				.setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsRep))//
				.setSafe(false)//
				.setIdempotent(false)//
				.build();

	}
	public static io.grpc.MethodDescriptor<Message, Message> createMethodDescriptor(String fullMehtodName,
			String methodName, Class<?> requestType, Class<?> responseType) {
		Message argsReq = createDefaultInstance(requestType);
		Message argsRep = createDefaultInstance(responseType);
		return io.grpc.MethodDescriptor.<Message, Message> newBuilder()
				.setType(io.grpc.MethodDescriptor.MethodType.UNARY)//
				.setFullMethodName(fullMehtodName)//
				.setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsReq))//
				.setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsRep))//
				.setSafe(false)//
				.setIdempotent(false)//
				.build();
	}

	public static Message createDefaultInstance(Class<?> type) {
		Class<? extends Message> messageType;
		if (!Message.class.isAssignableFrom(type)) {
			ProtobufEntity entity = (ProtobufEntity) ReflectUtils.findAnnotationFromClass(type, ProtobufEntity.class);
			messageType = entity.value();
		} else {
			messageType = (Class<? extends Message>) type;
		}
		Object obj = ReflectUtils.classInstance(messageType);
		return (Message) obj;
	}
	
	public static Map<String,Object> MDC(){
		return methodDescriptorCache;
	}
}
