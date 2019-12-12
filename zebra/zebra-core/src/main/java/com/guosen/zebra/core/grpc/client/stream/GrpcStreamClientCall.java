package com.guosen.zebra.core.grpc.client.stream;

import java.util.Map;

import com.google.protobuf.Message;
import com.guosen.zebra.core.grpc.client.GrpcCallOptions;
import com.guosen.zebra.core.grpc.client.GrpcRequest;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;

public interface GrpcStreamClientCall {

	public StreamObserver<Message> asyncClientStream(MethodDescriptor<Message, Message> method,
			StreamObserver<Message> responseObserver);

	public void asyncServerStream(MethodDescriptor<Message, Message> method, StreamObserver<Message> responseObserver,
			Message requestParam);

	public StreamObserver<Message> asyncBidiStream(MethodDescriptor<Message, Message> method,
			StreamObserver<Message> responseObserver);

	public static GrpcStreamClientCall create(final GrpcRequest request, final Map<String, Object> refUrl) {
		CallOptions callOptions = GrpcCallOptions.createCallOptions(refUrl);
		return new GrpcStreamClientCall() {

			@Override
			public StreamObserver<Message> asyncClientStream(MethodDescriptor<Message, Message> method,
					StreamObserver<Message> responseObserver) {
				boolean streamingResponse = false;
				Channel channel = null;
				try{
					channel = request.getChannel();
					ClientCall<Message, Message> call = channel.newCall(method, callOptions);
					CallToStreamObserverAdapter<Message, Message> adapter = new CallToStreamObserverAdapter<Message, Message>(
							call);
					ClientCall.Listener<Message> responseListener = new StreamObserverToCallListenerAdapter<Message, Message>(
							responseObserver, adapter, streamingResponse);
					startCall(call, responseListener, streamingResponse);
					return adapter;
				}finally{
					if(channel!=null){
						request.returnChannel(channel);
					}
				}
				
			}

			@Override
			public void asyncServerStream(MethodDescriptor<Message, Message> method,
					StreamObserver<Message> responseObserver, Message requestParam) {
				boolean streamingResponse = true;
				Channel channel = null;
				ClientCall<Message, Message> call =null;
				try{
					channel = request.getChannel();
					call = channel.newCall(method, callOptions);
					CallToStreamObserverAdapter<Message, Message> adapter = new CallToStreamObserverAdapter<Message, Message>(
							call);
					ClientCall.Listener<Message> responseListener = new StreamObserverToCallListenerAdapter<Message, Message>(
							responseObserver, adapter, streamingResponse);
					startCall(call, responseListener, streamingResponse);
					call.sendMessage(requestParam);
					call.halfClose();
				} catch (Throwable t) {
					if(call!=null)call.cancel(null, t);
					throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
				}finally{
					if(channel!=null){
						request.returnChannel(channel);
					}
				}
			}

			@Override
			public StreamObserver<Message> asyncBidiStream(MethodDescriptor<Message, Message> method,
					StreamObserver<Message> responseObserver) {
				boolean streamingResponse = true;
				Channel channel = null;
				try{
					channel = request.getChannel();
					ClientCall<Message, Message> call = channel.newCall(method, callOptions);
					CallToStreamObserverAdapter<Message, Message> adapter = new CallToStreamObserverAdapter<Message, Message>(
							call);
					ClientCall.Listener<Message> responseListener = new StreamObserverToCallListenerAdapter<Message, Message>(
							responseObserver, adapter, streamingResponse);
					startCall(call, responseListener, streamingResponse);
					return adapter;
				}finally{
					if(channel!=null){
						request.returnChannel(channel);
					}
				}
				
			}
		};

	}

	static void startCall(ClientCall<Message, Message> call, ClientCall.Listener<Message> responseListener,
			boolean streamingResponse) {
		call.start(responseListener, new Metadata());
		if (streamingResponse) {
			call.request(1);
		} else {
			call.request(2);
		}
	}

}
