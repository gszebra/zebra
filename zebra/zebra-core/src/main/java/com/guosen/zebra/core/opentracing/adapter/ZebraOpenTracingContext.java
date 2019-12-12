package com.guosen.zebra.core.opentracing.adapter;

import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.opentracing.TraceContext;

/**
 * Zebra OpenTracing上下文<br/>
 * 将zebra多个Context封装为一个给adapter的包使用，以便后续修改。
 */
final class ZebraOpenTracingContext {

    private ZebraOpenTracingContext(){}

    /**
     * Tracing开启值
     */
    private static final String TRACING_OPEN = "1";

    /**
     * 是否开启Tracing
     * @return true : 开启; false ：关闭
     */
    public static boolean isTracingOpen() {
        String traceFlag = RpcContext.getContext().getAttachment(ZebraConstants.ZEBRA_OPEN_TRACING);
        return TRACING_OPEN.equals(traceFlag);
    }

    /**
     * 获取traceId
     * @return traceId
     */
    public static String getTraceId() {
        return TraceContext.getTraceId();
    }

    /**
     * 获取ParentSpanId
     * @return 父span id
     */
    public static String getParentSpanId() {
        return TraceContext.getParentId();
    }
}
