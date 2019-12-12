package com.guosen.zebra.core.opentracing.adapter;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.guosen.zebra.core.opentracing.TraceAgent;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import zipkin2.Endpoint;

import java.util.Map;

/**
 * Zebra OpenTracing Span实现<br/>
 * Zebra使用zipkin2客户端，所以将相关操作委托到zipkin2的span相关操作
 */
class ZebraOpenTracingSpan implements Span {
    /**
     * 微服务名称
     */
    private static final Supplier<String> SERVICE_NAME = Suppliers.memoize(EtcdRegistry::getServiceName);

    private zipkin2.Span.Builder delegate = zipkin2.Span.newBuilder();

    /**
     * Span开始时间，单位毫秒
     */
    private long startMicros;

    public ZebraOpenTracingSpan(ZebraOpenTracingSpanBuilder zebraSpanBuilder) {
        startMicros = zebraSpanBuilder.getStartMicroseconds();

        delegate.id(zebraSpanBuilder.getSpanId())
                .name(zebraSpanBuilder.getOperationName())
                .traceId(zebraSpanBuilder.getTraceId())
                .parentId(zebraSpanBuilder.getParentSpanId())
                .timestamp(startMicros);

        Map<String, Object> tags = zebraSpanBuilder.getTags();
        if (tags != null) {
            for (Map.Entry<String, Object> tag : tags.entrySet()) {
                delegate.putTag(tag.getKey(), tag.getValue().toString());
            }
        }
    }

    @Override
    public void finish() {
        long finishMicros = System.currentTimeMillis() * 1000;

        finish(finishMicros);
    }

    @Override
    public void finish(long finishMicros) {
        long duration = finishMicros - startMicros;
        String serviceName = SERVICE_NAME.get();
        Endpoint localEndpoint = Endpoint.newBuilder()
                .serviceName(serviceName)
                .build();

        zipkin2.Span span = delegate.duration(duration)
                .localEndpoint(localEndpoint)
                .build();

        TraceAgent.getInstance().send(span);
    }

    @Override
    public SpanContext context() {
        return null;
    }

    @Override
    public Span setTag(String key, String value) {
        delegate.putTag(key, value);
        return this;
    }

    @Override
    public Span setTag(String key, boolean value) {
        delegate.putTag(key, String.valueOf(value));
        return this;
    }

    @Override
    public Span setTag(String key, Number value) {
        delegate.putTag(key, String.valueOf(value));
        return this;
    }

    @Override
    public Span log(Map<String, ?> fields) {
        return null;
    }

    @Override
    public Span log(long timestampMicroseconds, Map<String, ?> fields) {
        String timestampStr = String.valueOf(timestampMicroseconds);
        for (Map.Entry<String, ?> entry : fields.entrySet()) {
            String tagKey = timestampStr + "-" + entry.getKey();
            String value = entry.getValue().toString();

            delegate.putTag(tagKey, value);
        }
        return this;
    }

    @Override
    public Span log(String event) {
        return null;
    }

    @Override
    public Span log(long timestampMicroseconds, String event) {
        delegate.putTag(String.valueOf(timestampMicroseconds), event);
        return this;
    }

    @Override
    public Span setBaggageItem(String key, String value) {
        return null;
    }

    @Override
    public String getBaggageItem(String key) {
        return null;
    }

    @Override
    public Span setOperationName(String operationName) {
        delegate.name(operationName);
        return this;
    }

    @Override
    public Span log(String eventName, Object payload) {
        return null;
    }

    @Override
    public Span log(long timestampMicroseconds, String eventName, Object payload) {
        return null;
    }
}
