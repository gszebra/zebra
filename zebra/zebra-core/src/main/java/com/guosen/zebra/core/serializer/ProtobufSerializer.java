package com.guosen.zebra.core.serializer;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.guosen.zebra.core.exception.ProtobufAnnotationException;
import com.guosen.zebra.core.exception.ProtobufException;
import com.guosen.zebra.core.serializer.help.Pojo2ProtobufHelp;
import com.guosen.zebra.core.serializer.help.Protobuf2PojoHelp;
import com.guosen.zebra.core.serializer.utils.JReflectionUtils;
import com.guosen.zebra.core.serializer.utils.JStringUtils;
import com.guosen.zebra.core.serializer.utils.ProtobufSerializerUtils;

public class ProtobufSerializer implements IProtobufSerializer {

	/**
	 * @see com.guosen.zebra.core.serializer.IProtobufSerializer#toProtobuf(java.lang.Object)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Message toProtobuf(Object pojo) throws ProtobufException {
		try {
			final Class<?> fromClazz = (Class<?>) pojo.getClass();
			final Class<? extends GeneratedMessageV3> protoClazz = ProtobufSerializerUtils
					.getProtobufClassFromPojoAnno(fromClazz);
			if (protoClazz == null) {
				throw new ProtobufAnnotationException("Doesn't seem like " + fromClazz + " is ProtobufEntity");
			}
			final Message.Builder protoObjBuilder =(Message.Builder) JReflectionUtils.runStaticMethod(protoClazz, "newBuilder");
			List<FieldDescriptor> list = protoObjBuilder.getDescriptorForType().getFields();
			for (FieldDescriptor field : list) {
				final String fieldName = field.getName();
				Field f = fromClazz.getDeclaredField(fieldName);
				ProtobufAttribute gpbAnnotation  = f.getAnnotation(ProtobufAttribute.class);
				if(gpbAnnotation ==null) continue;
				Object value = Pojo2ProtobufHelp.getPojoFieldValue(pojo, gpbAnnotation, f);
				if (value == null) {
					continue;
				}
				boolean isAny= false;
				if(field.getJavaType() == JavaType.MESSAGE&&"google.protobuf.Any".equals(field.getMessageType().getFullName())){
					isAny = true;
				}
//				if(field.getMessageType().getContainingType())
				value = Pojo2ProtobufHelp.serializeToProtobufEntity(value,isAny);
				if (value instanceof Collection) {
					value = Pojo2ProtobufHelp.convertCollectionToProtobufs((Collection<Object>) value,isAny);
					if (value == null ||((Collection) value).isEmpty()) {
						continue;
					}
				}
				if (value instanceof Map) {
					value = Pojo2ProtobufHelp.convertMapToProtobufs((Map) value, isAny);
					if (value == null ||((Map) value).isEmpty()) {
						continue;
					}
					JReflectionUtils.runMethod(protoObjBuilder,
							protoObjBuilder.getClass().getDeclaredMethod("putAll" + JStringUtils.upperCaseFirst(field.getName()), Map.class), value);
					continue;
				}
				protoObjBuilder.setField(field, value);
			}
			return protoObjBuilder.build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ProtobufException("Could not generate Protobuf object for " + pojo.getClass() + ": " + e, e);
		}
	}

	/**
	 * @see com.guosen.zebra.core.serializer.IProtobufSerializer#fromProtobuf(com.google.protobuf.Message,
	 *      java.lang.Class)
	 */
	@Override
	public Object fromProtobuf(Message protobuf, Class<?> pojoClazz) throws ProtobufException {
		try {
			final Class<? extends Message> protoClazz = ProtobufSerializerUtils.getProtobufClassFromPojoAnno(pojoClazz);
			if (protoClazz == null) {
				throw new ProtobufAnnotationException("Doesn't seem like " + pojoClazz + " is ProtobufEntity");
			}
			final Map<Field, ProtobufAttribute> protobufFields =ProtobufSerializerUtils.getAllProtbufFields(pojoClazz);
			if (protobufFields.isEmpty()) {
				throw new ProtobufException(
						"No protoBuf fields have been annotated on the class " + pojoClazz + ", thus cannot continue.");
			}
			Object pojo = pojoClazz.newInstance();
			for (Entry<Field, ProtobufAttribute> entry : protobufFields.entrySet()) {
				final Field field = entry.getKey();
				final ProtobufAttribute protobufAttribute = entry.getValue();
				final String setter = ProtobufSerializerUtils.getPojoSetter(protobufAttribute, field);
				Object protobufValue = Protobuf2PojoHelp.getProtobufFieldValue(protobuf, protobufAttribute, field);
				if (protobufValue == null) {
					continue;
				}
				Protobuf2PojoHelp.setPojoFieldValue(pojo, setter, protobufValue, protobufAttribute);
			}
			return pojo;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ProtobufException("Could not generate POJO of type " + pojoClazz + " from Protobuf object "
					+ protobuf.getClass() + ": " + e, e);
		}
	}

}
