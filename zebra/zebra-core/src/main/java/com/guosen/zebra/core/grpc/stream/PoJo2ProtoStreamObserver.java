package com.guosen.zebra.core.grpc.stream;

import com.google.protobuf.Message;
import com.guosen.zebra.core.exception.ProtobufException;
import com.guosen.zebra.core.grpc.util.SerializerUtil;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.netty.util.internal.ThrowableUtil;

public class PoJo2ProtoStreamObserver implements StreamObserver<Object> {
	private final StreamObserver<Message> streamObserver;

	private PoJo2ProtoStreamObserver(StreamObserver<Message> streamObserver) {
		this.streamObserver = streamObserver;
	}

	public static PoJo2ProtoStreamObserver newObserverWrap(StreamObserver<Message> streamObserver) {
		return new PoJo2ProtoStreamObserver(streamObserver);
	}

	@Override
	public void onNext(Object value) {
		try {
			Object respPojo = value;
			Message respProtoBufer = SerializerUtil.pojo2Protobuf(respPojo);
			streamObserver.onNext(respProtoBufer);
		} catch (ProtobufException e) {
			String stackTrace = ThrowableUtil.stackTraceToString(e);
			StatusRuntimeException statusException = Status.UNAVAILABLE.withDescription(stackTrace)
					.asRuntimeException();
			streamObserver.onError(statusException);
		}
	}

	@Override
	public void onError(Throwable t) {
		streamObserver.onError(t);
	}

	@Override
	public void onCompleted() {
		streamObserver.onCompleted();
	}

}
