package com.guosen.zebra.maven.plugin;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;

public final class PrintServiceFile extends AbstractPrint {

  private Map<String, String> pojoTypeCache;

  private List<MethodDescriptorProto> serviceMethods;

  public PrintServiceFile(String fileRootPath, String sourcePackageName, String className) {
    super(fileRootPath, sourcePackageName, className);
  }

  public void setPojoTypeCache(Map<String, String> pojoTypeCache) {
    this.pojoTypeCache = pojoTypeCache;
  }

  public void setServiceMethods(List<MethodDescriptorProto> serviceMethods) {
    this.serviceMethods = serviceMethods;
  }

  @Override
  protected List<String> collectFileData() {
    String className = super.getClassName();
    String packageName = super.getSourcePackageName().toLowerCase();
    List<String> fileData = Lists.newArrayList();
    fileData.add("package " + packageName + ";");
    fileData.add("public interface " + className + "{");
    for (MethodDescriptorProto method : serviceMethods) {
      String outPutType = method.getOutputType();
      String inPutType = method.getInputType();
      String methodName = method.getName();
      inPutType = CommonUtils.findPojoTypeFromCache(inPutType, pojoTypeCache);
      outPutType = CommonUtils.findPojoTypeFromCache(outPutType, pojoTypeCache);
      String stream = generateGrpcStream(method, inPutType, outPutType);
      if (method.getServerStreaming() || method.getClientStreaming()) {
        outPutType = "io.grpc.stub.StreamObserver<" + outPutType + ">";
      }
      String inputValue = CommonUtils.findNotIncludePackageType(inPutType).toLowerCase();
      if (method.getClientStreaming()) {
        inPutType = "io.grpc.stub.StreamObserver<" + inPutType + ">";
      }
      if (stream != null)
        fileData.add(stream);
      String methodStr = generateMethod(method, inPutType, outPutType, methodName, inputValue);
      fileData.add(methodStr);
    }
    fileData.add("}");
    return fileData;
  }

  private String generateGrpcStream(MethodDescriptorProto method, String inPutType,
      String outPutType) {
    String format =
        "@com.guosen.zebra.core.grpc.anotation.GrpcMethodType(methodType=%s,requestType=%s,responseType=%s)";
    if (method.getServerStreaming() && method.getClientStreaming()) {
      String stream = String.format(format, "io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING",
          inPutType + ".class", outPutType + ".class");
      return stream;
    } else {
      if (!method.getServerStreaming() && method.getClientStreaming()) {
        String stream =
            String.format(format, "io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING",
                inPutType + ".class", outPutType + ".class");
        return stream;
      } else if (method.getServerStreaming() && !method.getClientStreaming()) {
        String stream =
            String.format(format, "io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING",
                inPutType + ".class", outPutType + ".class");
        return stream;
      }
      return String.format(format, "io.grpc.MethodDescriptor.MethodType.UNARY",
          inPutType + ".class", outPutType + ".class");
    }
  }

  private String generateMethod(MethodDescriptorProto method, String inPutType, String outPutType,
      String methodName, String inputValue) {
    String methodStr =
        "public " + outPutType + " " + methodName + "(" + inPutType + " " + inputValue + ");";
    boolean isClientStream = !method.getServerStreaming() && method.getClientStreaming();
    boolean isBidiStream = method.getServerStreaming() && method.getClientStreaming();
    boolean isServerStream = method.getServerStreaming() && !method.getClientStreaming();
    if (isClientStream || isBidiStream) {
      methodStr =
          "public " + inPutType + " " + methodName + "(" + outPutType + " responseObserver);";
    } else if (isServerStream) {
      methodStr = "public void " + methodName + "(" + inPutType + " " + inputValue + ","
          + outPutType + " responseObserver);";
    }
    return methodStr;
  }


}
