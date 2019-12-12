package com.guosen.zebra.monitor.metrics.server;

import com.guosen.zebra.monitor.metrics.Configuration;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

import java.time.Clock;
import java.time.Instant;

/**
 * An updating Prometheus metrics of server-side actions for a gateway request.
 */
public class MonitoringGatewayCall {

    @SuppressWarnings("unused")
	private static final Object mutex = new Object();
    @SuppressWarnings("unused")
	private volatile static ServerMetrics.Factory serverMetricsFactory;
    private final Clock clock;
    private final ServerMetrics serverMetrics;
    private final Configuration configuration;
    private final Instant startInstant;


    public MonitoringGatewayCall(String serviceName, String serviceMethod, MethodDescriptor.MethodType methodType,
                                 Configuration configuration) {
        this.clock = Clock.systemDefaultZone();
        this.serverMetrics = getMetricFactory(configuration).createMetricsForGWMethod(serviceName, serviceMethod, methodType);
        this.configuration = configuration;
        this.startInstant = clock.instant();

        reportStartMetrics();
    }

    private ServerMetrics.Factory getMetricFactory(Configuration configuration) {
        return ServerMetricsFactoryHolder.ALL_METRIC_FACTORY;
    }

    public void close(Status status) {
        reportEndMetrics(status);
    }

    private void reportStartMetrics() {
        serverMetrics.recordCallStarted();
    }

    private void reportEndMetrics(Status status) {
        serverMetrics.recordServerHandled(status.getCode());
        if (configuration.isIncludeLatencyHistograms()) {
            double latencySec = clock.millis() - startInstant.toEpochMilli();
            serverMetrics.recordLatency(latencySec);
        }
    }
}