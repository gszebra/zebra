package com.guosen.zebra.core.grpc.client.unary;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.Lists;
import com.google.protobuf.Message;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.client.GrpcCallOptions;
import com.guosen.zebra.core.grpc.client.GrpcRequest;

import io.grpc.CallOptions;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

@SuppressWarnings("unchecked")
public interface GrpcUnaryClientCall {
	public CompletionFuture<? extends Message> unaryFuture(Message request, MethodDescriptor<Message, Message> method);

	public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method);

	public CompletionFuture<InputStream> unaryIsFuture(InputStream request,
			MethodDescriptor<InputStream, InputStream> method);

	public InputStream blockingUnaryIsResult(InputStream request, MethodDescriptor<InputStream, InputStream> method);

	public static GrpcUnaryClientCall create(final GrpcRequest orgRequest, final Integer retryOptions,
			final Map<String, Object> refUrl) {
		CallOptions callOptions = GrpcCallOptions.createCallOptions(refUrl);
		return new GrpcUnaryClientCall() {
			@Override
			public CompletionFuture<Message> unaryFuture(Message request, MethodDescriptor<Message, Message> method) {
				UnaryCallListner<Message, Message> retryCallListener = new UnaryCallListner<Message, Message>(
						orgRequest, retryOptions, method, callOptions);
				retryCallListener.setRequest(request);
				retryCallListener.run();
				return retryCallListener.getFuture();
			}

			@Override
			public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method) {
				UnaryCallListner<Message, Message> retryCallListener = new UnaryCallListner<Message, Message>(
						orgRequest, retryOptions, method, callOptions);
				retryCallListener.setRequest(request);
				try {
					retryCallListener.run();
					Integer timeout = (Integer) ((HashMap<String, Object>) callOptions
							.getOption(GrpcCallOptions.CALLOPTIONS_CUSTOME_KEY).get(GrpcCallOptions.GRPC_REF_URL))
									.get(ZebraConstants.TIMEOUT);
					return retryCallListener.getFuture().get(timeout, TimeUnit.MILLISECONDS);
				} catch (Exception e) {
					e.printStackTrace();
					retryCallListener.cancel();
					throw Status.CANCELLED.withCause(e).asRuntimeException();
				} finally {
					if (ZebraConstants.IS_RECORD_TARGET_IP)
						RpcContext.getContext().set(GrpcCallOptions.GRPC_CURRENT_ADDR_KEY,
								Lists.newArrayList().add(((CompletionFuture<?>) retryCallListener.getFuture()).getRemote()));
				}
			}

			@Override
			public CompletionFuture<InputStream> unaryIsFuture(InputStream request,
					MethodDescriptor<InputStream, InputStream> method) {
				InputStreamUnaryCallListner<InputStream, InputStream> retryCallListener = new InputStreamUnaryCallListner<InputStream, InputStream>(
						orgRequest, retryOptions, method, callOptions);
				retryCallListener.setRequest(request);
				retryCallListener.run();
				return retryCallListener.getFuture();
			}

			@Override
			public InputStream blockingUnaryIsResult(InputStream request,
					MethodDescriptor<InputStream, InputStream> method) {
				InputStreamUnaryCallListner<InputStream, InputStream> retryCallListener = new InputStreamUnaryCallListner<InputStream, InputStream>(
						orgRequest, retryOptions, method, callOptions);
				retryCallListener.setRequest(request);
				try {
					retryCallListener.run();
					Integer timeout = (Integer) ((HashMap<String, Object>) callOptions
							.getOption(GrpcCallOptions.CALLOPTIONS_CUSTOME_KEY).get(GrpcCallOptions.GRPC_REF_URL))
									.get(ZebraConstants.TIMEOUT);
					return retryCallListener.getFuture().get(timeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					retryCallListener.cancel();
					throw Status.CANCELLED.withCause(e).asRuntimeException();
				} catch (ExecutionException e) {
					retryCallListener.cancel();
					throw Status.fromThrowable(e).asRuntimeException();
				} catch (TimeoutException e) {
					retryCallListener.cancel();
					throw Status.fromThrowable(e).asRuntimeException();
				} finally {
					if (ZebraConstants.IS_RECORD_TARGET_IP)
						RpcContext.getContext().set(GrpcCallOptions.GRPC_CURRENT_ADDR_KEY,
								Lists.newArrayList().add(((CompletionFuture<?>) retryCallListener.getFuture()).getRemote()));
				}
			}
		};
	}

}
