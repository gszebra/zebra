package com.guosen.zebra.monitor.metrics.server;

import com.guosen.zebra.monitor.metrics.GrpcMethod;
import io.grpc.ForwardingServerCallListener;
import io.grpc.ServerCall;

/**
 * A {@link ForwardingServerCallListener} which updates Prometheus metrics for a single rpc based
 * on updates received from grpc.
 */
public class MonitoringServerCallListener<R> extends ForwardingServerCallListener<R> {
    private final ServerCall.Listener<R> delegate;
    private final GrpcMethod grpcMethod;
    private final ServerMetrics serverMetrics;

    public  MonitoringServerCallListener(
            ServerCall.Listener<R> delegate, ServerMetrics serverMetrics, GrpcMethod grpcMethod) {
        this.delegate = delegate;
        this.serverMetrics = serverMetrics;
        this.grpcMethod = grpcMethod;
    }

    @Override
    protected ServerCall.Listener<R> delegate() {
        return delegate;
    }

    @Override
    public void onMessage(R request) {
        if (grpcMethod.streamsRequests()) {
            serverMetrics.recordStreamMessageReceived();
        }
        super.onMessage(request);
    }
}