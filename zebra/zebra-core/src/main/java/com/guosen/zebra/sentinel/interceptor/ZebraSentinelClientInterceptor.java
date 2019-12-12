/**   
* @Title: ZebraSentinelClientInterceptor.java 
* @Package com.guosen.zebra.sentinel.interceptor 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年12月11日 上午8:17:58 
* @version V1.0   
*/
package com.guosen.zebra.sentinel.interceptor;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.grpc.SentinelGrpcClientInterceptor;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.monitor.health.SentinelManager;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

/**
 * @ClassName: ZebraSentinelClientInterceptor
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2018年12月11日 上午8:17:58
 * 
 */
public class ZebraSentinelClientInterceptor extends SentinelGrpcClientInterceptor {

	private static final Status FLOW_CONTROL_BLOCK = Status.UNAVAILABLE
			.withDescription("Flow control limit exceeded (client side)");

	private static final Status BLACK_CONTROL_BLOCK = Status.UNAVAILABLE
			.withDescription("Black control limit exceeded (client side)");

	protected AbstractRule rule;

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
			CallOptions callOptions, Channel channel) {
		// Allow access, forward the call.
		String resourceName = methodDescriptor.getFullMethodName();
		Entry entry = null;
		try {
			ContextUtil.enter(resourceName);
			entry = SphU.entry(resourceName, EntryType.OUT);
			return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
					channel.newCall(methodDescriptor, callOptions)) {
				@Override
				public void start(Listener<RespT> responseListener, Metadata headers) {
					if (SentinelManager.isOpenGatewayLimit()) {
						String ip = RpcContext.getContext().getAttachments().get(ZebraConstants.IP_TAG);
						String hwid = RpcContext.getContext().getAttachments().get(ZebraConstants.HW_ID_TAG);
						List<AuthorityRule> list = AuthorityRuleManager.getRules();
						for (AuthorityRule rule : list) {
							String limits = rule.getLimitApp();
							if (StringUtils.isEmpty(limits))
								continue;
							String arrays[] = limits.split(",");
							for (String app : arrays) {
								if (app.equals(ip) || app.equals(hwid)) {
									responseListener.onClose(BLACK_CONTROL_BLOCK, new Metadata());
								}
							}
						}
					}
					super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {
						@Override
						public void onReady() {
							super.onReady();
						}

						@Override
						public void onClose(Status status, Metadata trailers) {
							super.onClose(status, trailers);
							// Record the exception metrics.
							if (!status.isOk()) {
								recordException(status.asRuntimeException());
							}
						}
					}, headers);
				}

				@Override
				public void cancel(@Nullable String message, @Nullable Throwable cause) {
					super.cancel(message, cause);
					// Record the exception metrics.
					recordException(cause);
				}
			};
		} catch (BlockException e) {
			// Flow control threshold exceeded, block the call.
			return new ClientCall<ReqT, RespT>() {
				@Override
				public void start(Listener<RespT> responseListener, Metadata headers) {
					responseListener.onClose(FLOW_CONTROL_BLOCK, new Metadata());
				}

				@Override
				public void request(int numMessages) {

				}

				@Override
				public void cancel(@Nullable String message, @Nullable Throwable cause) {

				}

				@Override
				public void halfClose() {

				}

				@Override
				public void sendMessage(ReqT message) {

				}
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
