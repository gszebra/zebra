package com.guosen.zebra.core.grpc.interceptor;

import java.time.Clock;

import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.grpc.GrpcEngine;
import com.guosen.zebra.monitor.metrics.Configuration;
import com.guosen.zebra.monitor.metrics.GrpcMethod;
import com.guosen.zebra.monitor.metrics.server.MonitoringServerCall;
import com.guosen.zebra.monitor.metrics.server.MonitoringServerCallListener;
import com.guosen.zebra.monitor.metrics.server.ServerMetrics;
import com.guosen.zebra.monitor.metrics.server.ServerMetricsFactoryHolder;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * A {@link ServerInterceptor} which sends stats about incoming grpc calls to
 * Prometheus.
 * Usage:
 * MonitoringServerInterceptor monitoringInterceptor =
 * MonitoringServerInterceptor.create(Configuration.simpleMetrics());
 * server = ServerBuilder.forPort(port)
 * .addService(ServerInterceptors.intercept(new GreeterImpl(), monitoringInterceptor))
 * .build().start();
 */
public class MonitoringServerInterceptor implements ServerInterceptor {
    private final Clock clock;
    private final Configuration configuration;
    private final ServerMetrics.Factory serverMetricsFactory;

    private MonitoringServerInterceptor(Clock clock, Configuration configuration,
                                        ServerMetrics.Factory serverMetricsFactory) {
        this.clock = clock;
        this.configuration = configuration;
        this.serverMetricsFactory = serverMetricsFactory;
    }

    public static MonitoringServerInterceptor create(Configuration configuration) {
        ServerMetrics.Factory factory = ServerMetricsFactoryHolder.ALL_METRIC_FACTORY;
        return new MonitoringServerInterceptor(Clock.systemDefaultZone(), configuration,
                factory);
    }

    @Override
    public <R, S> ServerCall.Listener<R> interceptCall(ServerCall<R, S> call, Metadata requestHeaders,
			ServerCallHandler<R, S> next) {
		MethodDescriptor<R, S> method = call.getMethodDescriptor();
		ServerMetrics metrics = serverMetricsFactory.createMetricsForZebraMethod(method);
		GrpcMethod grpcMethod = GrpcMethod.of(method);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ServerCall<R, S> monitoringCall = new MonitoringServerCall(call, clock, grpcMethod, metrics, configuration,
				GrpcEngine.getActiviteThreadCount(),ZebraConstants.MAX_POOL_SIZE);
		return new MonitoringServerCallListener<>(next.startCall(monitoringCall, requestHeaders), metrics,
				GrpcMethod.of(method));
	}


}
