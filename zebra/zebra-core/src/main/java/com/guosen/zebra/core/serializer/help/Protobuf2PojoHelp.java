package com.guosen.zebra.core.serializer.help;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.MapEntry;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import com.guosen.zebra.core.exception.JException;
import com.guosen.zebra.core.exception.ProtobufException;
import com.guosen.zebra.core.serializer.ProtobufAttribute;
import com.guosen.zebra.core.serializer.ProtobufEntity;
import com.guosen.zebra.core.serializer.ProtobufSerializer;
import com.guosen.zebra.core.serializer.utils.JReflectionUtils;
import com.guosen.zebra.core.serializer.utils.ProtobufSerializerUtils;
import com.guosen.zebra.core.serializer.utils.ReflectUtils;

public class Protobuf2PojoHelp {
	public static Map<String,Class<?>> anyTypeForProto  = Maps.newHashMap();
	public static Map<String,Class<?>> anyTypeForPoJo  = Maps.newHashMap();

	private Protobuf2PojoHelp() {

	}

	public static final Object serializeFromProtobufEntity(Message protoBuf, Class<?> pojoClazz) throws JException {
		final ProtobufEntity protoBufEntity = ProtobufSerializerUtils.getProtobufEntity(pojoClazz);
		if (protoBufEntity == null) {
			return protoBuf;
		}
		return new ProtobufSerializer().fromProtobuf(protoBuf, pojoClazz);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Object getProtobufFieldValue(Message protoBuf, ProtobufAttribute protobufAttribute, Field field)
			throws JException, InstantiationException, IllegalAccessException {
//		final String getter = ProtobufSerializerUtils.getProtobufGetter(protobufAttribute, field);
		// This is used to determine if the Protobuf message has populated this
		// value
		Boolean isCollection = Boolean.FALSE;
		if (Collection.class.isAssignableFrom(field.getType())) {
			isCollection = Boolean.TRUE;
		}
		FieldDescriptor fd = protoBuf.getDescriptorForType().findFieldByName(field.getName());
		Object protobufValue =protoBuf.getField(fd);
//		Object protobufValue = JReflectionUtils.runMethod(protoBuf, getter, (Object[]) null);
		if (isCollection && ((Collection) protobufValue).isEmpty()) {
			return null;
		}
		// If the field itself is a ProtbufEntity, serialize that!
		if (protobufValue instanceof Message && ProtobufSerializerUtils.isProtbufEntity(field.getType())) {
			protobufValue = serializeFromProtobufEntity((Message) protobufValue, field.getType());
		}
		if (protobufValue instanceof Collection) {
			if(fd.isMapField()){
				Map map = Maps.newHashMap();
				List<MapEntry<?, ?>> list = (List<MapEntry<?, ?>>) protobufValue;
				list.forEach(item ->{
					map.put(item.getKey(), item.getValue());
				});
				protobufValue = map;
			}else{
				protobufValue = convertCollectionFromProtobufs(field, (Collection<?>) protobufValue);
				if (((Collection) protobufValue).isEmpty()) {
					return null;
				}
			}
		}
		if (protobufValue instanceof ProtocolMessageEnum) {
			protobufValue = JReflectionUtils.runStaticMethod(field.getType(), "forNumber",
					((ProtocolMessageEnum) protobufValue).getNumber());
		}
		return protobufValue;
	}

	public static final void setPojoFieldValue(Object pojo, String setter, Object protobufValue,
			ProtobufAttribute protobufAttribute) throws InstantiationException, IllegalAccessException, JException {
		Class<? extends Object> argClazz = null;
		if (protobufValue instanceof List) {
			final ArrayList<Object> newCollectionValues = new ArrayList<>();
			newCollectionValues.addAll((Collection<?>) protobufValue);
			protobufValue = newCollectionValues;
			argClazz = ArrayList.class;
		} else if (protobufValue instanceof Map) {
			final Map<Object, Object> newMapValues = new HashMap<>();
			newMapValues.putAll((Map<?, ?>) protobufValue);
			protobufValue = newMapValues;
			argClazz = Map.class;
		} else {
			protobufValue.getClass();
		}
		JReflectionUtils.runSetter(pojo, setter, protobufValue, argClazz);
	}

	@SuppressWarnings("unchecked")
	private static Object convertCollectionFromProtobufs(Field field, Collection<?> collectionOfProtobufs)
			throws JException, InstantiationException, IllegalAccessException {
		if (collectionOfProtobufs.isEmpty()) {
			return collectionOfProtobufs;
		}
		final ParameterizedType listType = (ParameterizedType) field.getGenericType();
		final Class<?> collectionClazzType = (Class<?>) listType.getActualTypeArguments()[0];
		final ProtobufEntity protoBufEntityAnno = ProtobufSerializerUtils.getProtobufEntity(collectionClazzType);
		final Object first = collectionOfProtobufs.toArray()[0];
		if (!(first instanceof Message) && protoBufEntityAnno == null) {
			return collectionOfProtobufs;
		}
		final Collection<Object> newCollectionOfValues = new ArrayList<>();
		for (Object protobufValue : collectionOfProtobufs) {
			if (!(protobufValue instanceof Message)) {
				throw new ProtobufException("Collection contains an object of type " + protobufValue.getClass()
						+ " which is not an instanceof GeneratedMessage, can not (de)serialize this");
			}else if(protobufValue instanceof Any){
				String clazzName = ((Any)protobufValue).getTypeUrl().split("/")[0].split("#")[0];
				String pojoClazz = ((Any)protobufValue).getTypeUrl().split("/")[0].split("#")[1];
		        try {
		        	Class <Message>clz;
		        	Class <Object>pojoClz;
		        	if(anyTypeForProto.containsKey(clazzName)){
		        		clz = (Class<Message>) anyTypeForProto.get(clazzName);
		        	}else{
		        		clz = (Class<Message>) ReflectUtils.name2class(clazzName);
		        		anyTypeForProto.put(clazzName, clz);
		        	}
		        	
		        	if(anyTypeForPoJo.containsKey(pojoClazz)){
		        		pojoClz = (Class<Object>) anyTypeForPoJo.get(pojoClazz);
		        	}else{
		        		pojoClz = (Class<Object>) ReflectUtils.name2class(pojoClazz);
		        		anyTypeForPoJo.put(pojoClazz, pojoClz);
		        	}
					Message msg= ((Any)protobufValue).unpack(clz);
					newCollectionOfValues.add(serializeFromProtobufEntity(msg, pojoClz));
				} catch (Exception e) {
					e.printStackTrace();
					throw new ProtobufException("Proto Any Message type serialize failed");
				}
			}else{
				newCollectionOfValues.add(serializeFromProtobufEntity((Message) protobufValue, collectionClazzType));
			}
		}

		return newCollectionOfValues;
	}

}
