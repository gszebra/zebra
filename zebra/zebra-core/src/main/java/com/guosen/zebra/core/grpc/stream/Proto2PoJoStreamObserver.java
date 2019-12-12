package com.guosen.zebra.core.grpc.stream;

import com.google.protobuf.Message;
import com.guosen.zebra.core.exception.ProtobufException;
import com.guosen.zebra.core.grpc.util.SerializerUtil;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.netty.util.internal.ThrowableUtil;

public class Proto2PoJoStreamObserver implements StreamObserver<Message> {


  private final StreamObserver<Object> streamObserver;

  private final Class<?> poJoType;

  private Proto2PoJoStreamObserver(StreamObserver<Object> streamObserver, Class<?> poJoType) {
    this.streamObserver = streamObserver;
    this.poJoType = poJoType;
  }

  public static Proto2PoJoStreamObserver newObserverWrap(StreamObserver<Object> streamObserver,
      Class<?> returnType) {
    return new Proto2PoJoStreamObserver(streamObserver, returnType);
  }

  @Override
  public void onNext(Message value) {
    try {
      Object respPoJo = SerializerUtil.protobuf2Pojo(value, poJoType);
      streamObserver.onNext(respPoJo);
    } catch (ProtobufException e) {
      String stackTrace = ThrowableUtil.stackTraceToString(e);
      StatusRuntimeException statusException =
          Status.UNAVAILABLE.withDescription(stackTrace).asRuntimeException();
      streamObserver.onError(statusException);
    }
  }


  @Override
  public void onError(Throwable t) {
    streamObserver.onError(t);;
  }


  @Override
  public void onCompleted() {
    streamObserver.onCompleted();
  }

}
