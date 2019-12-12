package com.guosen.zebra.maven.plugin;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label;
import com.google.protobuf.UnknownFieldSet;

public final class PrintMessageFile extends AbstractPrint {

	private List<FieldDescriptorProto> messageFields;

	private Map<String, String> pojoTypeCache;

	private DescriptorProto sourceMessageDesc;

	public PrintMessageFile(String fileRootPath, String sourcePackageName, String className) {
		super(fileRootPath, sourcePackageName, className);
	}

	public void setMessageFields(List<FieldDescriptorProto> messageFields) {
		this.messageFields = messageFields;
	}

	public void setPojoTypeCache(Map<String, String> pojoTypeCache) {
		this.pojoTypeCache = pojoTypeCache;
	}

	public void setSourceMessageDesc(DescriptorProto sourceMessageDesc) {
		this.sourceMessageDesc = sourceMessageDesc;
	}

	@Override
	protected List<String> collectFileData() {
		String sourePackageName = super.getSourcePackageName();
		String className = super.getClassName();
		String packageName = sourePackageName.toLowerCase();
		List<String> packageData = Lists.newArrayList();
		packageData.add("package " + packageName + ";");
		packageData.add("");

		List<String> importData = Lists.newArrayList();
		importData.add("import com.guosen.zebra.core.serializer.ProtobufAttribute;");
		importData.add("import com.guosen.zebra.core.serializer.ProtobufEntity;");

		List<String> classAnnotationData = Lists.newArrayList();
		classAnnotationData.add("");
		classAnnotationData.add("@ProtobufEntity(" + sourePackageName + "." + className + ".class)");

		boolean validator = false;
		List<String> fileData = Lists.newArrayList();
		fileData.add("public class " + className + "{");
		for (int i = 0; i < messageFields.size(); i++) {
			FieldDescriptorProto messageField = messageFields.get(i);
			String javaType = findJavaType(packageName, sourceMessageDesc, messageField);
			if (messageField.getLabel() == Label.LABEL_REPEATED && javaType != null) {
				if (!javaType.contains("java.util.Map")) {
					javaType = "java.util.ArrayList<" + javaType + ">";
				}
			}
			fileData.add("");
			String fieldName = messageField.getName();
			UnknownFieldSet unknownFields = messageField.getOptions().getUnknownFields();
			if (unknownFields != null) {
				for (Map.Entry<Integer, UnknownFieldSet.Field> integerFieldEntry : unknownFields.asMap().entrySet()) {
					for (ByteString byteString : integerFieldEntry.getValue().getLengthDelimitedList()) {
						validator = true;
						String validateMsg = byteString.toStringUtf8();
						fileData.add("    " + validateMsg);
					}
				}
			}

			fileData.add("    @ProtobufAttribute");
			fileData.add("    private " + javaType + " " + fieldName + ";");
			fileData.add("");
			fileData.add("    public " + javaType + " get" + captureName(fieldName) + "() {");
			fileData.add("        return this." + fieldName + ";");
			fileData.add("    }");
			fileData.add("");
			fileData.add("    public void set" + captureName(fieldName) + "(" + javaType + " " + fieldName + ") {");
			fileData.add("        this." + fieldName + "=" + fieldName + ";");
			fileData.add("    }");
			fileData.add("");
		}
		fileData.add("}");
		if (validator) {
			importData.add("import com.guosen.zebra.core.grpc.anotation.ArgValidator;");
			classAnnotationData.add("@ArgValidator");
		}
		packageData.addAll(importData);
		packageData.addAll(classAnnotationData);
		packageData.addAll(fileData);
		return packageData;
	}

	private String findJavaType(String packageName, DescriptorProto sourceMessageDesc, FieldDescriptorProto field) {
		switch (field.getType()) {
		case TYPE_ENUM:
			return getMessageJavaType(packageName, sourceMessageDesc, field);
		case TYPE_MESSAGE:
			String javaType = getMessageJavaType(packageName, sourceMessageDesc, field);
			return javaType;
		case TYPE_GROUP:
			System.err.println("group have not support yet");
			return null;
		case TYPE_STRING:
			return "String";
		case TYPE_INT64:
			return "Long";
		case TYPE_INT32:
			return "Integer";
		case TYPE_FIXED64:
			return "Long";
		case TYPE_FIXED32:
			return "Integer";
		case TYPE_UINT64:
			return "Long";
		case TYPE_UINT32:
			return "Integer";
		case TYPE_SINT64:
			return "Long";
		case TYPE_SINT32:
			return "Integer";
		case TYPE_SFIXED64:
			return "Long";
		case TYPE_SFIXED32:
			return "Integer";
		case TYPE_BOOL:
			return "Boolean";
		case TYPE_DOUBLE:
			return "Double";
		case TYPE_FLOAT:
			return "Float";
		default:
			System.err.println("have not support this type " + field.getType());
			return "Object";
		}
	}

	private String getMessageJavaType(String packageName, DescriptorProto sourceMessageDesc,
			FieldDescriptorProto field) {
		String fieldType = CommonUtils.findNotIncludePackageType(field.getTypeName());
		Map<String, Pair<DescriptorProto, List<FieldDescriptorProto>>> nestedFieldType = transform(sourceMessageDesc);
		// isMap
		if (nestedFieldType.containsKey(fieldType)) {
			Pair<DescriptorProto, List<FieldDescriptorProto>> nestedFieldPair = nestedFieldType.get(fieldType);
			if (nestedFieldPair.getRight().size() == 2) {
				DescriptorProto mapSourceMessageDesc = nestedFieldPair.getLeft();
				List<FieldDescriptorProto> mapFieldList = nestedFieldPair.getRight();
				String nestedJavaType = "java.util.Map<"
						+ findJavaType(packageName, mapSourceMessageDesc, mapFieldList.get(0)) + ","
						+ findJavaType(packageName, mapSourceMessageDesc, mapFieldList.get(1)) + ">";
				return nestedJavaType;
			} else {
				return null;
			}
		} else if ("Any".equals(fieldType)) {
			return "Object";
		} else {
			return CommonUtils.findPojoTypeFromCache(field.getTypeName(), pojoTypeCache);
		}
	}

	private Map<String, Pair<DescriptorProto, List<FieldDescriptorProto>>> transform(
			DescriptorProto sourceMessageDesc) {
		Map<String, Pair<DescriptorProto, List<FieldDescriptorProto>>> nestedFieldMap = Maps.newHashMap();
		sourceMessageDesc.getNestedTypeList().forEach(new Consumer<DescriptorProto>() {
			public void accept(DescriptorProto t) {
				nestedFieldMap.put(t.getName(),
						new ImmutablePair<DescriptorProto, List<FieldDescriptorProto>>(t, t.getFieldList()));
			}

		});
		return nestedFieldMap;
	}

	private String captureName(String name) {
		if (Character.isUpperCase(name.charAt(0))) {
			return name;
		}
		char[] cs = name.toCharArray();
		cs[0] -= 32;
		return String.valueOf(cs);
	}
}
