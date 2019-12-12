package com.guosen.zebra.core.serializer.help;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.guosen.zebra.core.exception.JException;
import com.guosen.zebra.core.exception.ProtobufAnnotationException;
import com.guosen.zebra.core.serializer.ProtobufAttribute;
import com.guosen.zebra.core.serializer.ProtobufEntity;
import com.guosen.zebra.core.serializer.ProtobufSerializer;
import com.guosen.zebra.core.serializer.utils.JReflectionUtils;
import com.guosen.zebra.core.serializer.utils.ProtobufSerializerUtils;

public class Pojo2ProtobufHelp {
	public static List<Descriptor> anyType  =Lists.newArrayList();
	
	private Pojo2ProtobufHelp() {

	}

	public static final Object getPojoFieldValue(Object pojo, ProtobufAttribute protobufAttribute, Field field)
			throws ProtobufAnnotationException {
		final String getter = protobufAttribute.pojoGetter();

		Object value = null;
		if (!getter.isEmpty()) {
			try {
				return JReflectionUtils.runMethod(pojo, getter);
			} catch (Exception e) {
				throw new ProtobufAnnotationException(
						"Could not get a value for field " + field.getName() + " using configured getter of " + getter,
						e);
			}
		}

		try {
			value = JReflectionUtils.runGetter(pojo, field);
		} catch (Exception ee) {
			throw new ProtobufAnnotationException("Could not execute getter " + getter + " on class "
					+ pojo.getClass().getCanonicalName() + ": " + ee, ee);
		}

		if (value == null && protobufAttribute.required()) {
			throw new ProtobufAnnotationException("Required field " + field.getName() + " on class "
					+ pojo.getClass().getCanonicalName() + " is null");
		}

		return value;
	}

	public static final Object serializeToProtobufEntity(Object pojo,boolean isAny) throws JException {
		final ProtobufEntity protoBufEntity = ProtobufSerializerUtils.getProtobufEntity(pojo.getClass());
		if (protoBufEntity == null) {
			return pojo;
		}
		if(isAny){
			Message msg = (new ProtobufSerializer().toProtobuf(pojo));
			if(!anyType.contains(msg.getDescriptorForType())){
				anyType.add(msg.getDescriptorForType());
			}
			return Any.pack(msg,msg.getClass().getName() + "#" + pojo.getClass().getName());
		}
		return new ProtobufSerializer().toProtobuf(pojo);
	}

	public static final void setProtobufFieldValue(ProtobufAttribute protobufAttribute, Builder protoObjBuilder,
			String setter, Object fieldValue)
			throws NoSuchMethodException, SecurityException, ProtobufAnnotationException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<? extends Object> fieldValueClass = fieldValue.getClass();
		Class<? extends Object> gpbClass = fieldValueClass;
		// Need to convert the argument class from non-primitives to primitives,
		// as Protobuf uses these.
		gpbClass = ProtobufSerializerUtils.getProtobufClass(fieldValue, gpbClass);
		final Method gpbMethod = protoObjBuilder.getClass().getDeclaredMethod(setter, gpbClass);
		gpbMethod.invoke(protoObjBuilder, fieldValue);
	}

	/**
	 * 集合元素转化为Protobuf
	 */
	public static final Object convertCollectionToProtobufs(Collection<Object> collectionOfNonProtobufs,boolean isAny)
			throws JException {
		if (collectionOfNonProtobufs.isEmpty()) {
			return collectionOfNonProtobufs;
		}
		final Object first = collectionOfNonProtobufs.toArray()[0];
		if (!ProtobufSerializerUtils.isProtbufEntity(first)) {
			return collectionOfNonProtobufs;
		}
		final Collection<Object> newCollectionValues;
		if (collectionOfNonProtobufs instanceof Set) {
			newCollectionValues = new HashSet<>();
		} else {
			newCollectionValues = new ArrayList<>();
		}
		for (Object iProtobufGenObj : collectionOfNonProtobufs) {
			newCollectionValues.add(Pojo2ProtobufHelp.serializeToProtobufEntity(iProtobufGenObj,isAny));
		}
		return newCollectionValues;
	}

	/**
	 * Map元素转化为Protobuf
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Object convertMapToProtobufs(Map<?, ?> mapOfNonProtobufs,boolean isAny) throws JException {
		if (mapOfNonProtobufs.isEmpty()) {
			return mapOfNonProtobufs;
		}
		final Object keyFirst = mapOfNonProtobufs.keySet().toArray()[0];
		final Object valueFirst = mapOfNonProtobufs.get(keyFirst);
		if (!ProtobufSerializerUtils.isProtbufEntity(keyFirst)
				&& !ProtobufSerializerUtils.isProtbufEntity(valueFirst)) {
			return mapOfNonProtobufs;
		}
		final Map newMapValues = new HashMap<>();
		for (Map.Entry<?, ?> entry : mapOfNonProtobufs.entrySet()) {
			Object newMapValuesKey = Pojo2ProtobufHelp.serializeToProtobufEntity(entry.getKey(),isAny);
			Object newMapValuesValue = Pojo2ProtobufHelp.serializeToProtobufEntity(entry.getValue(),isAny);
			newMapValues.put(newMapValuesKey, newMapValuesValue);
		}
		return newMapValues;
	}

}
