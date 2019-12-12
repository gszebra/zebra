package com.guosen.zebra.core.grpc;

import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;

import io.grpc.Attributes;
import io.grpc.Internal;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

@Internal
public class GrpcNameResolverProvider extends NameResolverProvider {

	public static final Attributes.Key<Map<List<SocketAddress>, String>> GRPC_ADDRESS_GRPCURL_MAPPING = Attributes.Key.create("grpc-address-mapping");

	public static final Attributes.Key<List<SocketAddress>> REMOTE_ADDR_KEYS = Attributes.Key.create("remote-addresss");

	public static final Attributes.Key<NameResolver.Listener> NAMERESOVER_LISTENER = Attributes.Key
			.create("nameResolver-Listener");

	private final Map<String, Object> subscribe;

	public GrpcNameResolverProvider(Map<String, Object> subscribe) {
		this.subscribe = subscribe;
	}

	@Override
	protected boolean isAvailable() {
		return true;
	}

	@Override
	protected int priority() {
		return 5;
	}

	@Override
	public NameResolver newNameResolver(URI targetUri, Attributes params) {
		return new GrpcNameResolver(targetUri, params, subscribe);
	}

	@Override
	public String getDefaultScheme() {
		return null;
	}

}
