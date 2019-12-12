package com.guosen.zebra.core.grpc.client.unary;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.guosen.zebra.core.grpc.GrpcNameResolverProvider;
import com.guosen.zebra.core.grpc.client.GrpcCallOptions;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

public class NameResolverNotify {

	private static final Object LOCK = new Object();

	private static final NameResolverNotify notify = new NameResolverNotify();

	private NameResolverNotify() {
	}

	public static NameResolverNotify newNameResolverNotify() {
		synchronized (LOCK) {
			if (notify != null) {
				return notify;
			} else {
				return new NameResolverNotify();
			}
		}
	}

	private List<SocketAddress> registry_servers;

	private SocketAddress current_server;

	private NameResolver.Listener listener;

	private Attributes affinity;

	public void refreshAffinity(Map<String, Object> affinity,SocketAddress current_server) {
		Attributes nameresoveCache = (Attributes) affinity.get(GrpcCallOptions.GRPC_NAMERESOVER_ATTRIBUTES);
		this.current_server =current_server;
		this.registry_servers = nameresoveCache.get(GrpcNameResolverProvider.REMOTE_ADDR_KEYS);
		this.listener = nameresoveCache.get(GrpcNameResolverProvider.NAMERESOVER_LISTENER);
		this.affinity = nameresoveCache;
	}

	public void refreshChannel() {
		if(current_server ==null){
			return;
		}
		List<SocketAddress> serversCopy = Lists.newArrayList();
		if (listener != null && current_server != null && registry_servers != null) {
			InetSocketAddress currentSock = (InetSocketAddress) current_server;
			int serverSize = registry_servers.size();
			if (serverSize >= 2) {
				for (int i = 0; i < serverSize; i++) {
					InetSocketAddress inetSock = (InetSocketAddress) registry_servers.get(i);
					boolean hostequal = inetSock.getHostName().equals(currentSock.getHostName());
					boolean portequal = inetSock.getPort() == currentSock.getPort();
					if (!hostequal || !portequal) {
						serversCopy.add(inetSock);
					}
				}
			} else {
				serversCopy.addAll(registry_servers);
			}
			if (serversCopy.size() != 0) {
				notifyChannel(serversCopy);
			}
		}
	}

	public void resetChannel() {
		if (registry_servers != null) {
			notifyChannel(registry_servers);
		}
	}

	private void notifyChannel(List<SocketAddress> addresses) {
		if (listener != null && registry_servers != null) {
			EquivalentAddressGroup server = new EquivalentAddressGroup(addresses);
			listener.onAddresses(Collections.singletonList(server), affinity);
		}
	}

}
