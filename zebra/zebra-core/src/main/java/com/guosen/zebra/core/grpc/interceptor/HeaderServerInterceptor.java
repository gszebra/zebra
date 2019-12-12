package com.guosen.zebra.core.grpc.interceptor;

import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.util.GrpcUtil;
import com.guosen.zebra.core.grpc.util.PropertiesContent;
import com.guosen.zebra.core.grpc.util.SerializerUtil;
import com.guosen.zebra.core.grpc.util.SpringContextUtils;
import com.guosen.zebra.core.opentracing.ZebraServerTracing;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import zipkin2.Span;

public class HeaderServerInterceptor implements ServerInterceptor {

	private static final Logger log = LogManager.getLogger(HeaderServerInterceptor.class);
	
	private  Metadata.Key<Integer> statusKey = Metadata.Key.of("statusKey-bin", intMarshaller());
	
	private Map<String,String> licenseMap = Maps.newHashMap();
	
	private boolean enableAcl = false;
	
	public static ServerInterceptor instance() {
		return new HeaderServerInterceptor();
	}

	private HeaderServerInterceptor() {
		enableAcl = PropertiesContent.getbooleanValue("zebra.acl.server.enable");
		if(licenseMap.isEmpty()){
			int i = 0;
			while(true){
					String key = PropertiesContent.getStrValue("zebra.acl.client.clientName["+i+"]", null);
					if(StringUtils.isEmpty(key)){
						return;
					}
					String value = PropertiesContent.getStrValue("zebra.acl.client.clientlicense["+i+"]", null);
					licenseMap.put(key, value);
					i++;
			}
		}
	}

	@Override
	public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, final Metadata headers,
			ServerCallHandler<ReqT, RespT> next) {
		final ServerCall.Listener<ReqT> originalListener = next
				.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {
					@Override
					public void request(int numMessages) {
						InetSocketAddress remoteAddress = (InetSocketAddress) call.getAttributes()
								.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
						log.debug("FullMethodName:{},RemoteAddress={},attachments={},context={}",
								call.getMethodDescriptor().getFullMethodName(), remoteAddress.getHostString(),
								headers.get(GrpcUtil.GRPC_CONTEXT_ATTACHMENTS),
								headers.get(GrpcUtil.GRPC_CONTEXT_VALUES));
						super.request(numMessages);
					}

					@Override
					public void close(Status status, Metadata trailers) {
						headers.put(statusKey, status.getCode().value());
						delegate().close(status, trailers);
					}
					
					@Override
					public void sendHeaders(Metadata headers) {
					    delegate().sendHeaders(headers);
					}
				}, headers);
		return new ZebraServerListener<ReqT>(originalListener, headers,
				((InetSocketAddress) call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR)).getHostString(),
				call.getMethodDescriptor().getFullMethodName(),call);
	}
	
	private class ZebraServerListener<ReqT> extends ForwardingServerCallListener<ReqT> {
		private final ServerCall.Listener<ReqT> delegate;
		private final ServerCall<?, ?> call;
		private final Metadata headers;
		private final String remote;
		private final ZebraServerTracing serverTracing;
		private final String fullMethodName;
		private Span span;
		private Stopwatch watch ;

		public ZebraServerListener(ServerCall.Listener<ReqT> delegate, Metadata headers, String remote,String fullMethodName, ServerCall<?, ?> call) {
			this.delegate = delegate;
			this.headers = headers;
			this.remote = remote;
			serverTracing = SpringContextUtils.getBean(ZebraServerTracing.class);
			this.fullMethodName=fullMethodName;
			this.call = call;
		}

		@Override
		protected Listener<ReqT> delegate() {
			// TODO Auto-generated method stub
			return delegate;
		}

		@Override
		public void onMessage(ReqT message) {
			delegate.onMessage(message);
		}

		@Override
		public void onHalfClose() {
			copyMetadataToThreadLocal(headers,call);
			RpcContext.getContext().getAttachments().put(ZebraConstants.REMOTE_ADDRESS, remote);
			boolean isOpenTracing = "1"
					.equals(RpcContext.getContext().getAttachments().get(ZebraConstants.ZEBRA_OPEN_TRACING));
			if (isOpenTracing) {	
				try{
					span = serverTracing.startTrace(fullMethodName);
					watch = Stopwatch.createStarted();
				}catch(Exception e){
					log.warn(e.getMessage(),e);
				}
			}
			super.onHalfClose();
		}

		@Override
		public void onCancel() {
			super.onCancel();
		}

		@Override
		public void onComplete() {
			super.onComplete();
			if (span!=null) {
				try{
					serverTracing.endTrace(span,watch,headers.get(statusKey));
				}catch(Exception e){
					log.warn(e.getMessage(),e);
				}
			}
		}

		@Override
		public void onReady() {
			super.onReady();
		}

		private void copyMetadataToThreadLocal(Metadata headers,ServerCall<?, ?> call) {
			String attachments = headers.get(GrpcUtil.GRPC_CONTEXT_ATTACHMENTS);
			String values = headers.get(GrpcUtil.GRPC_CONTEXT_VALUES); 
			try {
				if (values != null) {
					Map<String, Object> valuesMap = SerializerUtil.fromJson(values,
							new TypeToken<Map<String, Object>>() {
							}.getType());
					if(enableAcl){//acl控制
						String key = (String) valuesMap.get("client");
						String lic = (String) valuesMap.get("license");
						if(licenseMap.get(key) == null||!licenseMap.get(key).equals(lic)){
							call.close(Status.PERMISSION_DENIED, headers);
							return;
						}
					}
					for (Map.Entry<String, Object> entry : valuesMap.entrySet()) {
						RpcContext.getContext().set(entry.getKey(), entry.getValue());
					}
				}
				if (attachments != null) {
					Map<String, String> attachmentsMap = SerializerUtil.fromJson(attachments,
							new TypeToken<Map<String, String>>() {
							}.getType());
					RpcContext.getContext().setAttachments(attachmentsMap);
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	private Metadata.BinaryMarshaller<Integer> intMarshaller() {
		return new Metadata.BinaryMarshaller<Integer>() {
			@Override
			public byte[] toBytes(Integer value) {
				return new byte[] {   
				        (byte) ((value >> 24) & 0xFF),   
				        (byte) ((value >> 16) & 0xFF),      
				        (byte) ((value >> 8) & 0xFF),      
				        (byte) (value & 0xFF)   
				    };   
			}

			@Override
			public Integer parseBytes(byte[] serialized) {
				return   serialized[3] & 0xFF |   
	            (serialized[2] & 0xFF) << 8 |   
	            (serialized[1] & 0xFF) << 16 |   
	            (serialized[0] & 0xFF) << 24;   
			}
		};
	}
}
