package com.guosen.zebra.core.grpc.client.stream;

import javax.annotation.Nullable;

import io.grpc.ClientCall;
import io.grpc.stub.ClientCallStreamObserver;

public class CallToStreamObserverAdapter<Request, Response> extends ClientCallStreamObserver<Request> {

	private boolean frozen;
	private final ClientCall<Request, ?> call;
	private Runnable onReadyHandler;
	private boolean autoFlowControlEnabled = true;

	public Runnable getOnReadyHandler() {
		return onReadyHandler;
	}

	public boolean isAutoFlowControlEnabled() {
		return autoFlowControlEnabled;
	}

	public CallToStreamObserverAdapter(ClientCall<Request, ?> call) {
		this.call = call;
	}

	public void freeze() {
		this.frozen = true;
	}

	@Override
	public void onNext(Request value) {
		call.sendMessage(value);
	}

	@Override
	public void onError(Throwable t) {
		call.cancel("Cancelled by client with StreamObserver.onError()", t);
	}

	@Override
	public void onCompleted() {
		call.halfClose();
	}

	@Override
	public boolean isReady() {
		return call.isReady();
	}

	@Override
	public void setOnReadyHandler(Runnable onReadyHandler) {
		if (frozen) {
			throw new IllegalStateException("Cannot alter onReadyHandler after call started");
		}
		this.onReadyHandler = onReadyHandler;
	}

	@Override
	public void disableAutoInboundFlowControl() {
		if (frozen) {
			throw new IllegalStateException("Cannot disable auto flow control call started");
		}
		autoFlowControlEnabled = false;
	}

	@Override
	public void request(int count) {
		call.request(count);
	}

	@Override
	public void setMessageCompression(boolean enable) {
		call.setMessageCompression(enable);
	}

	@Override
	public void cancel(@Nullable String message, @Nullable Throwable cause) {
		call.cancel(message, cause);
	}

}
