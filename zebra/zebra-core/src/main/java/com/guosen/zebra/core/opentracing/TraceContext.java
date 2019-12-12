package com.guosen.zebra.core.opentracing;

import com.guosen.zebra.core.common.RpcContext;

public class TraceContext{

    public static final String TRACE_ID_KEY = "traceId";

    public static final String SPAN_ID_KEY = "spanId";
    
    public static final String PARENT_ID_KEY = "parentId";
    
    public static final String SPAN_KEY = "span";
    
    public static final String WATCH_KEY = "watch";

    public static final String ANNO_CS = "cs";

    public static final String ANNO_CR = "cr";

    public static final String ANNO_SR = "sr";

    public static final String ANNO_SS = "ss";

    private TraceContext(){}

	public static void setTraceId(String traceId) {
		RpcContext.getContext().set(TRACE_ID_KEY, traceId);
	}

	public static String getTraceId() {
		return (String) RpcContext.getContext().get(TRACE_ID_KEY);
	}

	public static String getSpanId() {
		return (String) RpcContext.getContext().get(SPAN_ID_KEY);
	}
	
	public static String getParentId() {
		return (String) RpcContext.getContext().get(PARENT_ID_KEY);
	}

	public static void setSpanId(String spanId) {
		RpcContext.getContext().set(SPAN_ID_KEY, spanId);
	}
	
	public static void setParentId(String parentId) {
		RpcContext.getContext().set(PARENT_ID_KEY, parentId);
	}


    public static void clear(){
    	RpcContext.getContext().remove(TRACE_ID_KEY);
    	RpcContext.getContext().remove(SPAN_ID_KEY);
    	RpcContext.getContext().remove(PARENT_ID_KEY);
    }

    public static void start(){
        clear();
    }
}
