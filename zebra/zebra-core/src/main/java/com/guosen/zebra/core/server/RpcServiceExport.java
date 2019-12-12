package com.guosen.zebra.core.server;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import com.guosen.zebra.core.boot.autoconfigure.GrpcProperties;
import com.guosen.zebra.core.grpc.GrpcEngine;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;

/**
 * @ClassName: RpcServiceExport
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2017年10月31日 下午12:58:55
 * 
 */
public class RpcServiceExport {
	private static final Logger log = LogManager.getLogger(RpcServiceExport.class);
	private transient io.grpc.Server internalServer;

	private GrpcProperties grpcProperties;

	public void destroy() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (internalServer != null)
					log.info("*** shutting down gRPC server since JVM is shutting down");
				internalServer.shutdown();
				log.info("*** server shut down");
			}
		});
	}

	public synchronized void export(List<RpcServiceBaseInfo> rpcSeviceList, GrpcProperties grpcProperties,
			AbstractApplicationContext applicationContext) {
		this.setGrpcProperties(grpcProperties);
		try {
			internalServer = this.getGrpcEngine().getServer(rpcSeviceList, grpcProperties, applicationContext);
			internalServer.start();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					log.info("*** shutting down gRPC server since JVM is shutting down");
					internalServer.shutdown();
					log.info("*** server shut down");
				}
			});
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public GrpcEngine getGrpcEngine() {
		return GrpcEngineHolder.getInstance().getGrpcEngine(grpcProperties);
	}

	public GrpcProperties getGrpcProperties() {
		return grpcProperties;
	}

	public void setGrpcProperties(GrpcProperties grpcProperties) {
		this.grpcProperties = grpcProperties;
	}

	private static class GrpcEngineHolder {

		private static final AtomicReference<GrpcEngine> ENGINE = new AtomicReference<>();
		private static volatile GrpcEngineHolder INSTANCE;

		public synchronized static GrpcEngineHolder getInstance() {
			if (INSTANCE == null) {
				synchronized (GrpcEngineHolder.class) {
					if (INSTANCE == null) {
						INSTANCE = new GrpcEngineHolder();
					}
				}
			}
			return INSTANCE;
		}

		private GrpcEngine getGrpcEngine(GrpcProperties grpcProperties) {
			ENGINE.compareAndSet(null, new GrpcEngine(grpcProperties));
			return ENGINE.get();
		}
	}
}
