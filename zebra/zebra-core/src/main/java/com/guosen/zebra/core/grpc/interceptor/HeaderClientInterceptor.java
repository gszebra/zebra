package com.guosen.zebra.core.grpc.interceptor;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.util.GrpcUtil;
import com.guosen.zebra.core.grpc.util.PropertiesContent;
import com.guosen.zebra.core.grpc.util.SerializerUtil;
import com.guosen.zebra.core.grpc.util.SpringContextUtils;
import com.guosen.zebra.core.opentracing.ZebraClientTracing;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import zipkin2.Span;

public class HeaderClientInterceptor implements ClientInterceptor {

	private static final Logger log = LogManager.getLogger(HeaderClientInterceptor.class);
	private ZebraClientTracing clientTracing;
	private final String isOpenTracing = "1";
	private Map<String,String> licenseMap = Maps.newHashMap();
	
	public static ClientInterceptor instance() {
		return new HeaderClientInterceptor();
	}

	private HeaderClientInterceptor() {
		if(licenseMap.isEmpty()){
			int i = 0;
			while(true){
				String key = PropertiesContent.getStrValue("zebra.acl.client.serviceName["+i+"]", null);
				if(StringUtils.isEmpty(key)){
					return;
				}
				String value = PropertiesContent.getStrValue("zebra.acl.client.servicelicense["+i+"]", null);
				licenseMap.put(key, value);
				i++;
			}
		}
	}

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
			CallOptions callOptions, Channel next) {
		if (clientTracing == null)
			clientTracing = SpringContextUtils.getBean(ZebraClientTracing.class);
		return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
			// 判断API网关是否要打开调用链
			boolean isGatewayTracing = (isOpenTracing
					.equals(RpcContext.getContext().getAttachment(ZebraConstants.ZEBRA_OPEN_TRACING)) ? true : false);
			Stopwatch watch = null;
			Span span = null;

			@Override
			public void start(Listener<RespT> responseListener, Metadata headers) {
				if (isGatewayTracing) {
					try{
						span = clientTracing.startTrace(method.getFullMethodName());
						watch = Stopwatch.createStarted();
					}catch(Exception e){
						log.warn(e.getMessage(),e);
					}
					
				}
				copyThreadLocalToMetadata(method,headers);
				super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {
					@Override
					public void onHeaders(Metadata headers) {
						super.onHeaders(headers);
					}

					@Override
					public void onMessage(RespT message) {
						super.onMessage(message);
					}

					@Override
					public void onClose(Status status, Metadata trailers) {
						super.onClose(status, trailers);
						try{
							if (isGatewayTracing)
								clientTracing.endTrace(span, watch, status.getCode().value());
						}catch(Exception e){
							log.warn(e.getMessage(),e);
						}
					
					}
				}, headers);
			}
		};
	}

	private void copyThreadLocalToMetadata(MethodDescriptor<?, ?> method,Metadata headers) {
		Map<String, String> attachments = RpcContext.getContext().getAttachments();
		Map<String, Object> values = RpcContext.getContext().get();
		//acl控制
		if(!licenseMap.isEmpty()){
			values.put("client", EtcdRegistry.getServiceName());
			values.put("license", licenseMap.get(method.getFullMethodName().split("/")[0]));
		}
		try {
			if (!attachments.isEmpty()) {
				headers.put(GrpcUtil.GRPC_CONTEXT_ATTACHMENTS, SerializerUtil.toJson(attachments));
			}
			if (!values.isEmpty()) {
				headers.put(GrpcUtil.GRPC_CONTEXT_VALUES, SerializerUtil.toJson(values));
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
	}
}
