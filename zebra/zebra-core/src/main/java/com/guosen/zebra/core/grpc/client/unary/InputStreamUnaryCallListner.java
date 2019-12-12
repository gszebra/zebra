package com.guosen.zebra.core.grpc.client.unary;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.client.GrpcCallOptions;
import com.guosen.zebra.core.grpc.client.GrpcRequest;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

public class InputStreamUnaryCallListner<Request, Response> extends ClientCall.Listener<Response> implements Runnable {

	private static final Logger log = LogManager.getLogger(InputStreamUnaryCallListner.class);

	private final AtomicInteger currentRetries = new AtomicInteger(0);
	
	private final ScheduledExecutorService scheduleRetryService = io.grpc.internal.GrpcUtil.TIMER_SERVICE.create();	

	private Integer maxRetries;

	private MethodDescriptor<Request, Response> method;

	private CallOptions callOptions;

	private boolean enabledRetry;

	private CompletionFuture<Response> completionFuture;

	private ClientCall<Request, Response> clientCall;

	private Request request;

	private Response response;
	
	private GrpcRequest orgRequest;
	

	public InputStreamUnaryCallListner(final GrpcRequest request,final Integer retriesOptions,
			final MethodDescriptor<Request, Response> method, final CallOptions callOptions) {
		this.maxRetries = retriesOptions;
		this.method = method;
		this.callOptions = callOptions;
		this.orgRequest = request;
		this.enabledRetry = maxRetries > 0 ? true : false;
	}

	public InputStreamUnaryCallListner() {
		// TODO Auto-generated constructor stub
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(Response message) {
		if (this.response != null && !enabledRetry) {
			throw Status.INTERNAL.withDescription("More than one value received for unary call").asRuntimeException();
		}
		try {
			this.response = (Response) IOUtils.toBufferedInputStream((InputStream) message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClose(Status status, Metadata trailers) {
		try {
			SocketAddress remoteServer = clientCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
			if(ZebraConstants.IS_RECORD_TARGET_IP)
				getCompletionFuture().setRemote(remoteServer);
			if (status.isOk()) {
				statusOk(trailers);
			} else {
				statusError(status, trailers, remoteServer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void statusOk(Metadata trailers) {
		if (response == null) {
			getCompletionFuture().completeExceptionally(
					Status.INTERNAL.withDescription("No value received for unary call").asRuntimeException(trailers));
		}
		getCompletionFuture().complete(response);
	}

	private void statusError(Status status, Metadata trailers,SocketAddress remoteServer) {
		if (enabledRetry) {
			final NameResolverNotify nameResolverNotify = this.createNameResolverNotify(remoteServer);
			boolean retryHaveDone = this.retryHaveDone();
			if (retryHaveDone) {
				getCompletionFuture().completeExceptionally(status.asRuntimeException(trailers));
			} else {
				log.debug("status error begin retry");
				nameResolverNotify.refreshChannel();
				scheduleRetryService.execute(this);
				currentRetries.getAndIncrement();
				nameResolverNotify.resetChannel();
			}
		} else {
			getCompletionFuture().completeExceptionally(status.asRuntimeException(trailers));
		}

	}

	private NameResolverNotify createNameResolverNotify(SocketAddress remoteServer) {
		Map<String, Object> affinity = callOptions.getOption(GrpcCallOptions.CALLOPTIONS_CUSTOME_KEY);
		NameResolverNotify nameResolverNotify = NameResolverNotify.newNameResolverNotify();
		nameResolverNotify.refreshAffinity(affinity,remoteServer);
		return nameResolverNotify;
	}

	private boolean retryHaveDone() {
		return currentRetries.get() >= maxRetries;
	}

	@Override
	public void run() {
		Channel channel =null;
		try{
			channel =orgRequest.getChannel();
			this.clientCall = channel.newCall(method, callOptions);
			this.completionFuture = new CompletionFuture<Response>();
			this.clientCall.start(this, new Metadata());
			this.clientCall.sendMessage(request);
			this.clientCall.halfClose();
			this.clientCall.request(1);
//			if (ZebraConstants.IS_RECORD_TARGET_IP) {
//				completionFuture
//				.setTargetIp((SocketAddress) RpcContext.getContext().remove(GrpcCallOptions.GRPC_CURRENT_ADDR_KEY));
//			}
		}finally{
			if(channel!=null){
				orgRequest.returnChannel(channel);
			}
		}
	}

	public CompletionFuture<Response> getFuture() {
		return getCompletionFuture();
	}

	public void cancel() {
		if (clientCall != null) {
			clientCall.cancel("User requested cancelation.", null);
		}
	}

	private CompletionFuture<Response> getCompletionFuture() {
		return completionFuture;
	}

	public void setCompletionFuture(CompletionFuture<Response> completionFuture) {
		this.completionFuture = completionFuture;
	}

}
