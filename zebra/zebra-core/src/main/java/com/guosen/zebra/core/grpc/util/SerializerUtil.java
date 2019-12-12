package com.guosen.zebra.core.grpc.util;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.guosen.zebra.core.exception.ProtobufException;
import com.guosen.zebra.core.serializer.IProtobufSerializer;
import com.guosen.zebra.core.serializer.ProtobufSerializer;

public final class SerializerUtil {

	private static final Gson gson;

	private static final IProtobufSerializer serializer;

	static {
		serializer = new ProtobufSerializer();
		gson = new Gson();
	}

	private SerializerUtil() {
	}

	public static Message pojo2Protobuf(Object arg) throws ProtobufException {
		if (arg != null && !(arg instanceof Message)) {
			Message message = (Message) serializer.toProtobuf(arg);
			arg = null;
			return message;
		}
		return (Message) arg;
	}

	public static Object protobuf2Pojo(Message arg, Class<? extends Object> returnType) throws ProtobufException {
		if (arg == null) {
			return null;
		}
		if (!Message.class.isAssignableFrom(returnType)) {
			return serializer.fromProtobuf(arg, returnType);
		} else {
			return arg;
		}
	}

	public static String toJson(Object obj) {
		return gson.toJson(obj);
	}

	public static <T> T fromJson(String json, Type typeOfT) {
		return gson.fromJson(json, typeOfT);
	}

}
