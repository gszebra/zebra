package com.guosen.zebra.core.opentracing.adapter;

import com.guosen.zebra.core.grpc.util.Sequences;
import io.opentracing.*;

import java.util.LinkedHashMap;
import java.util.Map;

class ZebraOpenTracingSpanBuilder implements Tracer.SpanBuilder {
    private String operationName;
    private Map<String, Object> tags = new LinkedHashMap<>();
    private String traceId;
    private String parentSpanId;
    private long startMicroseconds;
    private String spanId;

    private ZebraOpenTracingSpanBuilder(){}

    public static ZebraOpenTracingSpanBuilder newBuilder(String operationName) {
        long startTime = System.currentTimeMillis() * 1000;

        ZebraOpenTracingSpanBuilder zebraSpanBuilder = new ZebraOpenTracingSpanBuilder();
        zebraSpanBuilder.operationName = operationName;
        zebraSpanBuilder.traceId = ZebraOpenTracingContext.getTraceId();
        zebraSpanBuilder.startMicroseconds = startTime;
        zebraSpanBuilder.spanId = Sequences.getPK();
        zebraSpanBuilder.parentSpanId = ZebraOpenTracingContext.getParentSpanId();
        return zebraSpanBuilder;
    }

    @Override
    public Tracer.SpanBuilder asChildOf(SpanContext parent) {
        return null;
    }

    @Override
    public Tracer.SpanBuilder asChildOf(BaseSpan<?> parent) {
        return null;
    }

    @Override
    public Tracer.SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
        return null;
    }

    @Override
    public Tracer.SpanBuilder ignoreActiveSpan() {
        return null;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, String value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, boolean value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, Number value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
        this.startMicroseconds = microseconds;
        return this;
    }

    @Override
    public ActiveSpan startActive() {
        // TODO 暂时未摸透ActiveSpan的实现，ActiveSpan在sharding-JDBC中用于统计连接数，可暂时忽略
        return NoopActiveSpanSource.INSTANCE.makeActive(null);
    }

    @Override
    public Span startManual() {
        return new ZebraOpenTracingSpan(this);
    }

    @Override
    public Span start() {
        return startManual();
    }


    public String getOperationName() {
        return operationName;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public long getStartMicroseconds() {
        return startMicroseconds;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }
}
