package com.guosen.zebra.monitor.metrics;

import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;

/**
 * Knows how to extract information about a single grpc method.
 */
public class GrpcMethod {
    private final String serviceName;
    private final String methodName;
    private final MethodType type;

    public GrpcMethod(String serviceName, String methodName, MethodType type) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.type = type;
    }

    public static GrpcMethod of(MethodDescriptor<?, ?> method) {
        String serviceName = MethodDescriptor.extractFullServiceName(method.getFullMethodName());

        // Full method name: "full.serviceName/MethodName". We extract the last part.
        String methodName = method.getFullMethodName().substring(serviceName.length() + 1);
        return new GrpcMethod(serviceName, methodName, method.getType());
    }

    /**
     * For Zebra, subtract "JSON" ending in serviceName.
     * @param method
     * @return
     */
    public static GrpcMethod ofZebraMethod(MethodDescriptor<?, ?> method) {
        String serviceName = MethodDescriptor.extractFullServiceName(method.getFullMethodName());

        // Full method name: "full.serviceName/MethodName". We extract the last part.
        String methodName = method.getFullMethodName().substring(serviceName.length() + 1);

        // For Zebra, subtract "JSON" ending in serviceName
        if (null != serviceName && serviceName.endsWith("JSON")) {
            final int endInx = serviceName.length() - 4; //subtract "JSON"
            if (endInx > 0) {
                serviceName = serviceName.substring(0, endInx);
            }

        }
        return new GrpcMethod(serviceName, methodName, method.getType());
    }

    public String serviceName() {
        return serviceName;
    }

    public String methodName() {
        return methodName;
    }

    public String type() {
        return type.toString();
    }

    public boolean streamsRequests() {
        return type == MethodType.CLIENT_STREAMING || type == MethodType.BIDI_STREAMING;
    }

    public boolean streamsResponses() {
        return type == MethodType.SERVER_STREAMING || type == MethodType.BIDI_STREAMING;
    }

}
