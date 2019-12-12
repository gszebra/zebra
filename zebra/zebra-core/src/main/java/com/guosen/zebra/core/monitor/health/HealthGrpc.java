package com.guosen.zebra.core.monitor.health;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.4.0)",
    comments = "Source: example/health.proto")
public final class HealthGrpc {

  private HealthGrpc() {}

  public static final String SERVICE_NAME = "com.guosen.zebra.core.monitor.health.Health";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest,
      com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse> METHOD_CHECK =
      io.grpc.MethodDescriptor.<com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest, com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "com.guosen.zebra.core.monitor.health.Health", "Check"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest,
      com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse> METHOD_GET_METHODS =
      io.grpc.MethodDescriptor.<com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest, com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "com.guosen.zebra.core.monitor.health.Health", "getMethods"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest,
      com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse> METHOD_GET_METRICS =
      io.grpc.MethodDescriptor.<com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest, com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "com.guosen.zebra.core.monitor.health.Health", "getMetrics"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.guosen.zebra.core.monitor.health.ServiceParam.SentinelRequest,
      com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse> METHOD_SET_SENTINEL =
      io.grpc.MethodDescriptor.<com.guosen.zebra.core.monitor.health.ServiceParam.SentinelRequest, com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "com.guosen.zebra.core.monitor.health.Health", "setSentinel"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.guosen.zebra.core.monitor.health.ServiceParam.SentinelRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse.getDefaultInstance()))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static HealthStub newStub(io.grpc.Channel channel) {
    return new HealthStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static HealthBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new HealthBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static HealthFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new HealthFutureStub(channel);
  }

  /**
   */
  public static abstract class HealthImplBase implements io.grpc.BindableService {

    /**
     */
    public void check(com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest request,
        io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CHECK, responseObserver);
    }

    /**
     */
    public void getMethods(com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest request,
        io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_METHODS, responseObserver);
    }

    /**
     */
    public void getMetrics(com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest request,
        io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_METRICS, responseObserver);
    }

    /**
     */
    public void setSentinel(com.guosen.zebra.core.monitor.health.ServiceParam.SentinelRequest request,
        io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SET_SENTINEL, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_CHECK,
            asyncUnaryCall(
              new MethodHandlers<
                com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest,
                com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse>(
                  this, METHODID_CHECK)))
          .addMethod(
            METHOD_GET_METHODS,
            asyncUnaryCall(
              new MethodHandlers<
                com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest,
                com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse>(
                  this, METHODID_GET_METHODS)))
          .addMethod(
            METHOD_GET_METRICS,
            asyncUnaryCall(
              new MethodHandlers<
                com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest,
                com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse>(
                  this, METHODID_GET_METRICS)))
          .addMethod(
            METHOD_SET_SENTINEL,
            asyncUnaryCall(
              new MethodHandlers<
                com.guosen.zebra.core.monitor.health.ServiceParam.SentinelRequest,
                com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse>(
                  this, METHODID_SET_SENTINEL)))
          .build();
    }
  }

  /**
   */
  public static final class HealthStub extends io.grpc.stub.AbstractStub<HealthStub> {
    private HealthStub(io.grpc.Channel channel) {
      super(channel);
    }

    private HealthStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HealthStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new HealthStub(channel, callOptions);
    }

    /**
     */
    public void check(com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest request,
        io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CHECK, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMethods(com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest request,
        io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_METHODS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMetrics(com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest request,
        io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_METRICS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void setSentinel(com.guosen.zebra.core.monitor.health.ServiceParam.SentinelRequest request,
        io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SET_SENTINEL, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class HealthBlockingStub extends io.grpc.stub.AbstractStub<HealthBlockingStub> {
    private HealthBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private HealthBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HealthBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new HealthBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse check(com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CHECK, getCallOptions(), request);
    }

    /**
     */
    public com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse getMethods(com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_METHODS, getCallOptions(), request);
    }

    /**
     */
    public com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse getMetrics(com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_METRICS, getCallOptions(), request);
    }

    /**
     */
    public com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse setSentinel(com.guosen.zebra.core.monitor.health.ServiceParam.SentinelRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SET_SENTINEL, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class HealthFutureStub extends io.grpc.stub.AbstractStub<HealthFutureStub> {
    private HealthFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private HealthFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected HealthFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new HealthFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse> check(
        com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CHECK, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse> getMethods(
        com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_METHODS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse> getMetrics(
        com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_METRICS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse> setSentinel(
        com.guosen.zebra.core.monitor.health.ServiceParam.SentinelRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SET_SENTINEL, getCallOptions()), request);
    }
  }

  private static final int METHODID_CHECK = 0;
  private static final int METHODID_GET_METHODS = 1;
  private static final int METHODID_GET_METRICS = 2;
  private static final int METHODID_SET_SENTINEL = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final HealthImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(HealthImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CHECK:
          serviceImpl.check((com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest) request,
              (io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse>) responseObserver);
          break;
        case METHODID_GET_METHODS:
          serviceImpl.getMethods((com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest) request,
              (io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse>) responseObserver);
          break;
        case METHODID_GET_METRICS:
          serviceImpl.getMetrics((com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest) request,
              (io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse>) responseObserver);
          break;
        case METHODID_SET_SENTINEL:
          serviceImpl.setSentinel((com.guosen.zebra.core.monitor.health.ServiceParam.SentinelRequest) request,
              (io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class HealthDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.guosen.zebra.core.monitor.health.ServiceParam.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (HealthGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new HealthDescriptorSupplier())
              .addMethod(METHOD_CHECK)
              .addMethod(METHOD_GET_METHODS)
              .addMethod(METHOD_GET_METRICS)
              .addMethod(METHOD_SET_SENTINEL)
              .build();
        }
      }
    }
    return result;
  }
}
