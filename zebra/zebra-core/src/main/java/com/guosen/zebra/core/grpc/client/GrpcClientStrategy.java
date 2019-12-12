package com.guosen.zebra.core.grpc.client;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.guosen.zebra.core.common.ZebraConstants;

import io.grpc.stub.AbstractStub;

public class GrpcClientStrategy {
	private static final Logger log = LogManager.getLogger(GrpcClientStrategy.class);
	private final GrpcProtocolClient<Object> grpcClient;

	private final GrpcProtocolClient.ChannelCall call;

	private final int callType;

	private final int callTimeout;

	public GrpcClientStrategy(Map<String, Object> params, GrpcProtocolClient.ChannelCall call) {
		this.call = call;
		this.callType = (Boolean) params.get(ZebraConstants.ASYNC_KEY) ? 1 : 2;
		this.callTimeout = (Integer) params.get(ZebraConstants.TIMEOUT);
		this.grpcClient = buildProtoClient(params);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private GrpcProtocolClient<Object> buildProtoClient(Map<String, Object> params) {
		log.debug("**************buildProtoClient,params = {}",params);
		boolean isGeneric = (Boolean) params.get(ZebraConstants.GENERIC_KEY);
		boolean isGrpcStub = (Boolean) params.get(ZebraConstants.GRPC_STUB_KEY);
		if (isGeneric) {
			return new GenericProxyClient<Object>(params);
		} else {
			if (isGrpcStub) {
				try {
					Class<? extends AbstractStub> stubClass = (Class<? extends AbstractStub>) params.get(ZebraConstants.INTERFACECLASS_KEY);
					return new GrpcStubClient<Object>(stubClass, params);
				} catch (Exception e) {
					throw new IllegalArgumentException("grpc stub client the class must exist in classpath", e);
				}
			} else {
				return new DefaultProxyClient<Object>(params);
			}
		}
	}

	public Object getGrpcClient() {
		return grpcClient.getGrpcClient(call, callType, callTimeout);
	}
}
