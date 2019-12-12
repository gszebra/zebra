package com.guosen.zebra.core.opentracing.adapter;

import io.opentracing.NoopSpanBuilder;
import io.opentracing.Tracer;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *  ZebraOpenTracingTracer测试类
 */
public class ZebraOpenTracingTracerTest {

    @Mocked
    private ZebraOpenTracingContext zebraOpenTracingContext;

    /**
     * 测试Tracing开启的情况
     */
    @Test
    public void testTracingOpen() {
        new Expectations() {
            {
                ZebraOpenTracingContext.isTracingOpen();
                result = true;
            }
        };

        Tracer tracer = ZebraOpenTracingTracer.getInstance();
        Tracer.SpanBuilder tracerBuilder = tracer.buildSpan("myOperation");

        assertThat(tracerBuilder.getClass(), equalTo(ZebraOpenTracingSpanBuilder.class));
    }

    /**
     * 测试Tracing关闭的情况
     */
    @Test
    public void testTracingClose() {
        new Expectations() {
            {
                ZebraOpenTracingContext.isTracingOpen();
                result = false;
            }
        };

        Tracer tracer = ZebraOpenTracingTracer.getInstance();
        Tracer.SpanBuilder tracerBuilder = tracer.buildSpan("myOperation");

        assertThat(tracerBuilder, equalTo(NoopSpanBuilder.INSTANCE));
    }
}
