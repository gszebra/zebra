package com.guosen.zebra.core.grpc;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;
import com.guosen.zebra.core.registry.etcd.EtcdRegistry;
import com.guosen.zebra.core.registry.etcd.NotifyListener;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.Internal;
import io.grpc.NameResolver;
import io.grpc.Status;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.SharedResourceHolder;

@Internal
public class GrpcNameResolver extends NameResolver {
	private static final Logger log = LogManager.getLogger(GrpcNameResolver.class);

	private final EtcdRegistry etcdRegistry = new  EtcdRegistry();
	
	private final Map<String, Object> subscribe;

	private volatile Map<RpcServiceBaseInfo, List<String>> urls = Maps.newConcurrentMap();
	private volatile Map<RpcServiceBaseInfo, List<SocketAddress>> socketAddresses = Maps.newConcurrentMap();

	private final NotifyListener.NotifyServiceListener serviceListener = new NotifyListener.NotifyServiceListener() {
		@Override
		public void notify(RpcServiceBaseInfo serviceInfo, List<String> urls) {
			if (log.isDebugEnabled()) {
				log.debug("Grpc nameresolve started listener,Receive notify from registry, prividerUrl is"
						+ Arrays.toString(urls.toArray()));
			}
			GrpcNameResolver.this.urls.put(serviceInfo, urls);
			notifyLoadBalance(serviceInfo, urls);
		}
	};

	private boolean shutdown;

	private boolean resolving;

	private Listener listener;

	private ExecutorService executor;

	public GrpcNameResolver(URI targetUri, Attributes params, Map<String, Object> subscribe) {
		this.executor = (ExecutorService) SharedResourceHolder.get(GrpcUtil.SHARED_CHANNEL_EXECUTOR);
		this.subscribe = subscribe;
	}

	@Override
	public final String getServiceAuthority() {
		return "grpc";
	}

	@Override
	public final synchronized void start(Listener listener) {
		Preconditions.checkState(this.listener == null, "already started");
		this.listener = listener;
		this.listener = Preconditions.checkNotNull(listener, "listener");
		resolve();
	}

	@Override
	public final synchronized void refresh() {
		Preconditions.checkState(listener != null, "not started");
		if (resolving || shutdown) {
			return;
		}
		executor.execute(refreshRunnable);
	}

	private void resolve() {
		if (resolving || shutdown) {
			return;
		}
		executor.execute(resolutionRunnable);
	}

	private final Runnable resolutionRunnable = new Runnable() {
		@Override
		public void run() {
			synchronized (GrpcNameResolver.this) {
				if (shutdown) {
					return;
				}
				resolving = true;
			}
			try {
				etcdRegistry.subscribe(subscribe, serviceListener);
			} finally {
				synchronized (GrpcNameResolver.this) {
					resolving = false;
				}
			}
		}
	};
	private final Runnable refreshRunnable = new Runnable() {
		@Override
		public void run() {
			synchronized (GrpcNameResolver.this) {
				if (shutdown) {
					return;
				}
				resolving = true;
			}
			try {
				etcdRegistry.refresh(subscribe, serviceListener);
			} finally {
				synchronized (GrpcNameResolver.this) {
					resolving = false;
				}
			}
		}
	};

	@Override
	public void shutdown() {
		if (shutdown) {
			return;
		}
		shutdown = true;
	}

	/**** help method *****/
	private void notifyLoadBalance(RpcServiceBaseInfo serviceInfo, List<String> urls) {
		if (urls != null && !urls.isEmpty()) {
			List<EquivalentAddressGroup> servers = Lists.newArrayList();
			List<SocketAddress> addresses = Lists.newArrayList();
			Map<List<SocketAddress>, String> addressUrlMapping = Maps.newHashMap();
			for (String url : urls) {
				if(url.split(":").length!=2) continue;
				String host = url.split(":")[0];
				int port = Integer.parseInt(url.split(":")[1]);
				List<SocketAddress> hostAddressMapping;
				hostAddressMapping = IpResolved(servers, addresses, host, port);
				addressUrlMapping.put(hostAddressMapping, url);
			}
			this.socketAddresses.put(serviceInfo, addresses);
			Attributes config = this.buildAttributes(serviceInfo, addressUrlMapping);
			GrpcNameResolver.this.listener.onAddresses(servers, config);
		} else {
			GrpcNameResolver.this.listener.onError(Status.NOT_FOUND.withDescription(
					"There is no service[" + serviceInfo.getService() + ", group=" + serviceInfo.getGroup() + ", set="
							+ serviceInfo.getSet() + ", version=" + serviceInfo.getVersion() + "] registy in etcd3"));
		}
	}

	private List<SocketAddress> IpResolved(List<EquivalentAddressGroup> servers, List<SocketAddress> addresses,
			String host, int port) {
		List<SocketAddress> hostAddressMapping = Lists.newArrayList();
		SocketAddress sock = new InetSocketAddress(InetAddresses.forString(host), port);
		hostAddressMapping.add(sock);
		addSocketAddress(servers, addresses, sock);
		return hostAddressMapping;
	}

	private void addSocketAddress(List<EquivalentAddressGroup> servers, List<SocketAddress> addresses,
			SocketAddress sock) {
		EquivalentAddressGroup server = new EquivalentAddressGroup(sock);
		servers.add(server);
		addresses.add(sock);
	}

	private Attributes buildAttributes(RpcServiceBaseInfo serviceInfo, Map<List<SocketAddress>, String> addressUrlMapping) {
		Attributes.Builder builder = Attributes.newBuilder();
		if (listener != null) {
			builder.set(GrpcNameResolverProvider.NAMERESOVER_LISTENER, listener);
		}
		if (socketAddresses.get(serviceInfo) != null) {
			builder.set(GrpcNameResolverProvider.REMOTE_ADDR_KEYS, socketAddresses.get(serviceInfo));
		}
		if (!addressUrlMapping.isEmpty()) {
			builder.set(GrpcNameResolverProvider.GRPC_ADDRESS_GRPCURL_MAPPING, addressUrlMapping);
		}
		return builder.build();
	}

}
