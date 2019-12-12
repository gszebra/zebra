package com.guosen.zebra.core.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.alibaba.ttl.TransmittableThreadLocal;

public class RpcContext{
	private static final TransmittableThreadLocal<RpcContext> LOCAL = new TransmittableThreadLocal<RpcContext>() {
		@Override
		protected RpcContext initialValue() {
			return new RpcContext();
		}
	};

	private final Map<String, String> attachments = new HashMap<String, String>();
	private final Map<String, Object> values = new HashMap<String, Object>();
	private CompletableFuture<?> future;
	private CompletableFuture<?> cfResult;

	public static RpcContext getContext() {
		return LOCAL.get();
	}
	
	public static void removeContext() {
		LOCAL.remove();
	}
	
	public static void resetContext(RpcContext value) {
		LOCAL.remove();
		LOCAL.set(value);
	}

	private RpcContext() {
	}
	public CompletableFuture<?> getFuture() {
		return future;
	}
	
	public void setFuture(CompletableFuture<?> future) {
		this.future = future;
	}
	
	public String getAttachment(String key) {
		return attachments.get(key)==null?"": attachments.get(key);
	}

	public boolean containAttachment(String key) {
		return attachments.containsKey(key);
	}

	public RpcContext setAttachment(String key, String value) {
		if (value == null) {
			attachments.remove(key);
		} else {
			attachments.put(key, value);
		}
		return this;
	}

	public RpcContext removeAttachment(String key) {
		attachments.remove(key);
		return this;
	}

	public Map<String, String> getAttachments() {
		return attachments;
	}

	public RpcContext setAttachments(Map<String, String> attachment) {
		this.attachments.clear();
		if (attachment != null && attachment.size() > 0) {
			this.attachments.putAll(attachment);
		}
		return this;
	}

	public void clear() {
		this.attachments.clear();
		this.values.clear();
	}

	public Map<String, Object> get() {
		return values;
	}

	public RpcContext set(String key, Object value) {
		if (value == null) {
			values.remove(key);
		} else {
			values.put(key, value);
		}
		return this;
	}

	public Object remove(String key) {
		return values.remove(key);
	}

	public Object get(String key) {
		return values.get(key);
	}

	public boolean contain(String key) {
		return values.containsKey(key);
	}

	public void setCfResult(final CompletableFuture<?> cfResult) {
		this.cfResult = cfResult;
	}
	
	public CompletableFuture<?> getCfResult() {
		return cfResult;
	}

	public Object retDeferredResult(){
		return null;
	}

}
