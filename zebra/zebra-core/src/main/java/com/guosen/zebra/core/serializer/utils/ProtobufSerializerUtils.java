package com.guosen.zebra.core.serializer.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.GeneratedMessageV3;
import com.guosen.zebra.core.serializer.ProtobufAttribute;
import com.guosen.zebra.core.serializer.ProtobufEntity;

public final class ProtobufSerializerUtils {

	public static final Class<? extends Object> getProtobufClass(Object value, Class<? extends Object> protobufClass) {
		if (value instanceof Integer) {
			return Integer.TYPE;
		}
		if (value instanceof Boolean) {
			return Boolean.TYPE;
		}
		if (value instanceof Double) {
			return Double.TYPE;
		}
		if (value instanceof Long || value instanceof Date) {
			return Long.TYPE;
		}
		if (value instanceof List) {
			return Iterable.class;
		}
		if (value instanceof Map) {
			return Map.class;
		}
		return protobufClass;
	}

	public static final ProtobufEntity getProtobufEntity(Class<?> clazz) {
		final ProtobufEntity protoBufEntity = clazz.getAnnotation(ProtobufEntity.class);
		if (protoBufEntity != null) {
			return protoBufEntity;
		}
		return null;
	}

	public static final boolean isProtbufEntity(Object object) {
		return isProtbufEntity(object.getClass());
	}

	public static final boolean isProtbufEntity(Class<?> clazz) {
		final ProtobufEntity protoBufEntity = getProtobufEntity(clazz);
		if (protoBufEntity != null) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static final Class<? extends GeneratedMessageV3> getProtobufClassFromPojoAnno(Class<?> clazz) {
		final ProtobufEntity annotation = getProtobufEntity(clazz);
		final Class<? extends GeneratedMessageV3> gpbClazz = (Class<? extends GeneratedMessageV3>) annotation.value();
		if (gpbClazz == null) {
			return null;
		}
		return gpbClazz;
	}

	public static final Map<Field, ProtobufAttribute> getAllProtbufFields(Class<? extends Object> fromClazz) {
		Map<Field, ProtobufAttribute> protoBufFields = new HashMap<>();
		final List<Field> fields = JReflectionUtils.getAllFields(new ArrayList<Field>(), fromClazz);
		for (Field field : fields) {
			final Annotation annotation = field.getAnnotation(ProtobufAttribute.class);
			if (annotation == null) {
				continue;
			}
			final ProtobufAttribute gpbAnnotation = (ProtobufAttribute) annotation;
			protoBufFields.put(field, gpbAnnotation);
		}
		return protoBufFields;
	}

	public static final String getProtobufSetter(ProtobufAttribute protobufAttribute, Field field, Object fieldValue) {
		String isUnderline []  = field.getName().split("_");
		String fieldName = "";
		for (int i = 0; i < isUnderline.length; i++) {
			fieldName +=  JStringUtils.upperCaseFirst(isUnderline[i]);
		}
		String setter = "set" + JStringUtils.upperCaseFirst(fieldName);
		if (fieldValue instanceof Collection) {
			setter = "addAll" + JStringUtils.upperCaseFirst(fieldName);
		}
		if (fieldValue instanceof Map) {
			setter = "putAll" + JStringUtils.upperCaseFirst(fieldName);
		}
		final String configedSetter = protobufAttribute.protobufSetter();
		if (!configedSetter.equals(JStringUtils.EMPTY)) {
			setter = configedSetter;
		}
		return setter;
	}

	public static final String getProtobufGetter(ProtobufAttribute protobufAttribute, Field field) {
		String isUnderline []  = field.getName().split("_");
		String fieldName = "";
		for (int i = 0; i < isUnderline.length; i++) {
			fieldName +=  JStringUtils.upperCaseFirst(isUnderline[i]);
		}
		String getter = "get" + fieldName;

		if (Collection.class.isAssignableFrom(field.getType())) {
			getter += "List";
		}
		if (!protobufAttribute.protobufGetter().isEmpty()) {
			return protobufAttribute.protobufGetter();
		}
		return getter;
	}

	public static final String getPojoSetter(ProtobufAttribute protobufAttribute, Field field) {
		final String upperCaseFirstFieldName = JStringUtils.upperCaseFirst(field.getName());
		String setter = "set" + upperCaseFirstFieldName;
		if (!protobufAttribute.pojoSetter().isEmpty()) {
			return protobufAttribute.pojoSetter();
		}
		return setter;
	}
}
