/**   
* @Title: ZebraSentinelServerInterceptor.java 
* @Package com.guosen.zebra.sentinel.interceptor 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年12月10日 下午4:41:33 
* @version V1.0   
*/
package com.guosen.zebra.sentinel.interceptor;

import java.net.InetSocketAddress;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.grpc.SentinelGrpcServerInterceptor;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.Status;

/**
 * @ClassName: ZebraSentinelServerInterceptor
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2018年12月10日 下午4:41:33
 * 
 */
public class ZebraSentinelServerInterceptor extends SentinelGrpcServerInterceptor {
	private static final Status FLOW_CONTROL_BLOCK = Status.UNAVAILABLE
			.withDescription("Flow control limit exceeded (server side)");

	@Override
	public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata,
			ServerCallHandler<ReqT, RespT> serverCallHandler) {
		String resourceName = serverCall.getMethodDescriptor().getFullMethodName();
		Entry entry = null;
		try {
			InetSocketAddress remoteAddress = (InetSocketAddress) serverCall.getAttributes()
					.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
			ContextUtil.enter(resourceName, remoteAddress.getHostString());
			entry = SphU.entry(resourceName, EntryType.IN);
			// Allow access, forward the call.
			return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(serverCallHandler
					.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(serverCall) {
						@Override
						public void close(Status status, Metadata trailers) {
							super.close(status, trailers);
							// Record the exception metrics.
							if (!status.isOk()) {
								recordException(status.asRuntimeException());
							}
						}
					}, metadata)) {
			};
		} catch (BlockException e) {
			serverCall.close(FLOW_CONTROL_BLOCK, new Metadata());
			return new ServerCall.Listener<ReqT>() {
			};
		} finally {
			if (entry != null) {
				entry.exit();
			}
			ContextUtil.exit();
		}
	}

	private void recordException(Throwable t) {
		Tracer.trace(t);
	}
}
