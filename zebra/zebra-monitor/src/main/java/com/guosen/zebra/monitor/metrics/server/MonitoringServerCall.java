package com.guosen.zebra.monitor.metrics.server;

import com.guosen.zebra.monitor.metrics.Configuration;
import com.guosen.zebra.monitor.metrics.GrpcMethod;
import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;

import java.time.Clock;
import java.time.Instant;

/**
 * A {@link ForwardingServerCall} updating Prometheus metrics of server-side actions for a single rpc.
 */
public class MonitoringServerCall<R, S> extends ForwardingServerCall.SimpleForwardingServerCall<R, S> {

    private final Clock clock;
    private final GrpcMethod grpcMethod;
    private final ServerMetrics serverMetrics;
    private final Configuration configuration;
    private final Instant startInstant;

    public MonitoringServerCall(ServerCall<R, S> delegate, Clock clock, GrpcMethod grpcMethod, ServerMetrics serverMetrics,
                         Configuration configuration,int currenThreadCount,int totalThreadCount) {
        super(delegate);
        this.clock = clock;
        this.grpcMethod = grpcMethod;
        this.serverMetrics = serverMetrics;
        this.configuration = configuration;
        this.startInstant = clock.instant();
        serverMetrics.recordThreadStat(new Double(currenThreadCount),totalThreadCount);
        reportStartMetrics();
    }

    @Override
    public void close(Status status, Metadata responseHeaders) {
        reportEndMetrics(status);
        super.close(status, responseHeaders);
    }

    @Override
    public void sendMessage(S message) {
        if (grpcMethod.streamsResponses()) {
            serverMetrics.recordStreamMessageSent();
        }
        super.sendMessage(message);
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