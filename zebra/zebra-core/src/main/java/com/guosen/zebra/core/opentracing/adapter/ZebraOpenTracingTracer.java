package com.guosen.zebra.core.opentracing.adapter;

import io.opentracing.*;
import io.opentracing.propagation.Format;

/**
 * Zebra OpenTracing Tracer实现
 */
public final class ZebraOpenTracingTracer implements Tracer {

    private ZebraOpenTracingTracer(){}

    /**
     * 获取Zebra OpenTracing Tracer实例
     */
    public static ZebraOpenTracingTracer getInstance() {
        return new ZebraOpenTracingTracer();
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        if (ZebraOpenTracingContext.isTracingOpen()) {
            return ZebraOpenTracingSpanBuilder.newBuilder(operationName);
        }
        else {
            // 没有开启Tracing，返回一个Noop对象，后续各种tracing操作都会变成空操作。
            return NoopSpanBuilder.INSTANCE;
        }
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        // TODO 待有需要再实现
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        // TODO 待有需要再实现
        return NoopSpan.INSTANCE.context();
    }

    @Override
    public ActiveSpan activeSpan() {
        // TODO 待有需要再实现
        return NoopActiveSpanSource.INSTANCE.makeActive(null);
    }

    @Override
    public ActiveSpan makeActive(Span span) {
        // TODO 待有需要再实现
        return NoopActiveSpanSource.INSTANCE.makeActive(null);
    }
}
