package com.guosen.zebra.core.opentracing;

import com.google.common.base.Stopwatch;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.util.NetUtils;
import com.guosen.zebra.core.grpc.util.Sequences;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;
import org.springframework.stereotype.Component;
import zipkin2.Endpoint;
import zipkin2.Span;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ServerTracing
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2018年5月7日 下午3:42:16
 * 
 */
@Component
public class ZebraServerTracing {

	public Span startTrace(String method) {
		String traceId = (String) RpcContext.getContext().get(TraceContext.TRACE_ID_KEY);
		String parentSpanId = (String) RpcContext.getContext().get(TraceContext.SPAN_ID_KEY);

		String id = Sequences.getPK();
		TraceContext.start();
		TraceContext.setTraceId(traceId);
//		TraceContext.setSpanId(id);
		TraceContext.setParentId(id);

		long timestamp = System.currentTimeMillis() * 1000;
		Endpoint endpoint = Endpoint.newBuilder().ip(NetUtils.getLocalHost())
				.serviceName(EtcdRegistry.getServiceName()).port(50003).build();
		String[] splitMethod = method.split("/");
		Span providerSpan = Span.newBuilder().id(id).parentId(parentSpanId).traceId(traceId)
				.name("local call :"+splitMethod[splitMethod.length - 1]).timestamp(timestamp).localEndpoint(endpoint)
				.addAnnotation(timestamp, TraceContext.ANNO_SR).putTag("className", EtcdRegistry.getServiceName())
				.putTag("method", splitMethod[splitMethod.length - 1])
				.putTag(ZebraConstants.USER_TAG, RpcContext.getContext().getAttachment(ZebraConstants.USER_TAG))
				.putTag(ZebraConstants.SESSION_TAG, RpcContext.getContext().getAttachment(ZebraConstants.SESSION_TAG))
				.putTag(ZebraConstants.PKG_TAG, RpcContext.getContext().getAttachment(ZebraConstants.PKG_TAG))
				.putTag(ZebraConstants.H_PKG_TAG, RpcContext.getContext().getAttachment(ZebraConstants.H_PKG_TAG))
				.putTag(ZebraConstants.N_PKG_TAG, RpcContext.getContext().getAttachment(ZebraConstants.N_PKG_TAG))
				.putTag(ZebraConstants.HW_ID_TAG, RpcContext.getContext().getAttachment(ZebraConstants.HW_ID_TAG))
				.build();
		return providerSpan;
	}

	public void endTrace(Span span, Stopwatch watch, int code) {
		span = span.toBuilder().addAnnotation(System.currentTimeMillis() * 1000, TraceContext.ANNO_SS)
				.duration(watch.stop().elapsed(TimeUnit.MICROSECONDS)).putTag("code", code + "").build();

		TraceAgent.getInstance().send(span);
	}
}
