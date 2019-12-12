package com.guosen.zebra.core.opentracing;

import com.google.common.base.Stopwatch;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.util.IdWorker;
import com.guosen.zebra.core.grpc.util.NetUtils;
import com.guosen.zebra.core.grpc.util.Sequences;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import zipkin2.Endpoint;
import zipkin2.Span;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ClientTracing
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2018年5月7日 下午3:11:29
 * 
 */
@Component
public class ZebraClientTracing {
	private IdWorker idWorker = IdWorker.getSingleton();

	public Span startTrace(String method) {
		String traceId = null;
		if (StringUtils.isEmpty(TraceContext.getTraceId())) {
			TraceContext.start();
			traceId = idWorker.nextId(true);
		} else {
			traceId = TraceContext.getTraceId();
		}
		String id =  Sequences.getPK();
		long timestamp = System.currentTimeMillis() * 1000;
		// 注册本地信息
		Endpoint endpoint = Endpoint.newBuilder().ip(NetUtils.getLocalHost())
				.serviceName(EtcdRegistry.getServiceName()).port(50003).build();
		// 初始化span
		String parentId = (StringUtils.isEmpty(TraceContext.getParentId()) ? "": TraceContext.getParentId());
		String[] splitMethod = method.split("/");
		Span consumerSpan = Span.newBuilder().localEndpoint(endpoint).id(id).traceId(traceId).parentId(parentId)
				.name("remote call :"+method).timestamp(timestamp)
				.addAnnotation(timestamp, TraceContext.ANNO_CS).putTag("className", EtcdRegistry.getServiceName())
				.putTag("method", splitMethod[splitMethod.length - 1])
				.putTag(ZebraConstants.USER_TAG, RpcContext.getContext().getAttachment(ZebraConstants.USER_TAG))
				.putTag(ZebraConstants.SESSION_TAG, RpcContext.getContext().getAttachment(ZebraConstants.SESSION_TAG))
				.putTag(ZebraConstants.PKG_TAG, RpcContext.getContext().getAttachment(ZebraConstants.PKG_TAG))
				.putTag(ZebraConstants.H_PKG_TAG, RpcContext.getContext().getAttachment(ZebraConstants.H_PKG_TAG))
				.putTag(ZebraConstants.N_PKG_TAG, RpcContext.getContext().getAttachment(ZebraConstants.N_PKG_TAG))
				.putTag(ZebraConstants.HW_ID_TAG, RpcContext.getContext().getAttachment(ZebraConstants.HW_ID_TAG))
				.build();
		// 将tracingid和spanid放到上下文
		TraceContext.setTraceId(consumerSpan.traceId());
		TraceContext.setSpanId(consumerSpan.id());
		return consumerSpan;
	}

	public void endTrace(Span span, Stopwatch watch, int code) {
		span = span.toBuilder().addAnnotation(System.currentTimeMillis() * 1000, TraceContext.ANNO_CR)
				.duration(watch.stop().elapsed(TimeUnit.MICROSECONDS)).putTag("code", code + "").build();

		TraceAgent.getInstance().send(span);
	}
}
