package com.guosen.zebra.monitor.metrics.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.guosen.zebra.monitor.metrics.Configuration;
import com.guosen.zebra.monitor.metrics.GrpcMethod;
import com.guosen.zebra.monitor.utils.Constants;

import io.grpc.MethodDescriptor;
import io.grpc.Status.Code;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.SimpleCollector;

/**
 * Prometheus metric definitions used for server-side monitoring of grpc services.
 */
public class ServerMetrics {
    private static final Counter.Builder serverStartedBuilder = Counter.build()
            .namespace("grpc")
            .subsystem("server")
            .name("started_total")
//            .labelNames("grpc_type", "grpc_service", "grpc_method")
            .labelNames("grpc_type", "grpc_service", "grpc_method", "instance")
            .help("Total number of RPCs started on the server.");

    private static final Counter.Builder serverHandledBuilder = Counter.build()
            .namespace("grpc")
            .subsystem("server")
            .name("handled_total")
//            .labelNames("grpc_type", "grpc_service", "grpc_method", "code")
            .labelNames("grpc_type", "grpc_service", "grpc_method", "instance", "code")
            .help("Total number of RPCs completed on the server regardless of success or failure.");

    private static final Histogram.Builder serverHandledLatencyMillisBuilder =
            Histogram.build()
                    .namespace("grpc")
                    .subsystem("server")
                    .name("handling_ms")
//                    .labelNames("grpc_type", "grpc_service", "grpc_method")
                    .labelNames("grpc_type", "grpc_service", "grpc_method", "instance")
                    .help("Histogram of response latency milliseconds of gRPC that had been application-level handled by the server.");

    private static final Counter.Builder serverStreamMessagesReceivedBuilder = Counter.build()
            .namespace("grpc")
            .subsystem("server")
            .name("msg_received_total")
//            .labelNames("grpc_type", "grpc_service", "grpc_method")
            .labelNames("grpc_type", "grpc_service", "grpc_method", "instance")
            .help("Total number of stream messages received from the client.");

    private static final Counter.Builder serverStreamMessagesSentBuilder = Counter.build()
            .namespace("grpc")
            .subsystem("server")
            .name("msg_sent_total")
//            .labelNames("grpc_type", "grpc_service", "grpc_method")
            .labelNames("grpc_type", "grpc_service", "grpc_method", "instance")
            .help("Total number of stream messages sent by the server.");
    
    /** 
    * @Fields inprogressRequests : 记录线程池数量
    */ 
    private static final Gauge.Builder currentThreadCountBuilder = Gauge.build()
    		.namespace("grpc")
            .subsystem("server")
            .name("active_thread_count")
            .labelNames("grpc_type", "grpc_service", "grpc_method", "instance", "total")
            .help("Total number of active threads.");

    private final Counter serverStarted;
    private final Counter serverHandled;
    private final Counter serverStreamMessagesReceived;
    private final Counter serverStreamMessagesSent;
    private final Optional<Histogram> serverHandledLatencyMillis;
    private final Gauge currenThreadCount;

    private final GrpcMethod method;

    private final String instance;

    private ServerMetrics(
            GrpcMethod method,
            Counter serverStarted,
            Counter serverHandled,
            Counter serverStreamMessagesReceived,
            Counter serverStreamMessagesSent,
            Optional<Histogram> serverHandledLatencyMillis,
            Gauge currenThreadCount) {
        this.method = method;
        this.serverStarted = serverStarted;
        this.serverHandled = serverHandled;
        this.serverStreamMessagesReceived = serverStreamMessagesReceived;
        this.serverStreamMessagesSent = serverStreamMessagesSent;
        this.serverHandledLatencyMillis = serverHandledLatencyMillis;
        this.currenThreadCount = currenThreadCount;
        this.instance = Constants.getInstanceIP();

    }


    public void recordCallStarted() {
        addLabels(serverStarted).inc();
    }

    public void recordServerHandled(Code code) {
        addLabels(serverHandled, code.toString()).inc();
    }

    public void recordStreamMessageSent() {
        addLabels(serverStreamMessagesSent).inc();
    }

    public void recordStreamMessageReceived() {
        addLabels(serverStreamMessagesReceived).inc();
    }
    
    public void recordThreadStat(double val, int totalThreadCount){
    	 addLabels(currenThreadCount,totalThreadCount+"").set(val);
    }

    /**
     * Only has any effect if monitoring is configured to include latency histograms. Otherwise, this
     * does nothing.
     */
    public void recordLatency(double latencySec) {
        if (!this.serverHandledLatencyMillis.isPresent()) {
            return;
        }
        addLabels(this.serverHandledLatencyMillis.get()).observe(latencySec);
    }

    private <T> T addLabels(SimpleCollector<T> collector, String... labels) {
        List<String> allLabels = new ArrayList<>();
        Collections.addAll(allLabels, method.type(), method.serviceName(), method.methodName(), instance);
        Collections.addAll(allLabels, labels);
        return collector.labels(allLabels.toArray(new String[0]));
    }

    /**
     * Knows how to produce {@link ServerMetrics} instances for individual methods.
     */
 public  static class Factory {
        private final Counter serverStarted;
        private final Counter serverHandled;
        private final Counter serverStreamMessagesReceived;
        private final Counter serverStreamMessagesSent;
        private final Optional<Histogram> serverHandledLatencySeconds;
        private final Gauge currentThreadCount;

        Factory(Configuration configuration) {
            CollectorRegistry registry = configuration.getCollectorRegistry();
            this.serverStarted = serverStartedBuilder.register(registry);
            this.serverHandled = serverHandledBuilder.register(registry);
            this.serverStreamMessagesReceived = serverStreamMessagesReceivedBuilder.register(registry);
            this.serverStreamMessagesSent = serverStreamMessagesSentBuilder.register(registry);
            this.currentThreadCount = currentThreadCountBuilder.register(registry);
            if (configuration.isIncludeLatencyHistograms()) {
                this.serverHandledLatencySeconds = Optional.of(serverHandledLatencyMillisBuilder
                        .buckets(configuration.getLatencyBuckets())
                        .register(registry));
            } else {
                this.serverHandledLatencySeconds = Optional.empty();
            }
        }

        /**
         * Creates a {@link ServerMetrics} for the supplied method.
         */
        public <R, S> ServerMetrics createMetricsForMethod(MethodDescriptor<R, S> methodDescriptor) {
            return new ServerMetrics(
                    GrpcMethod.of(methodDescriptor),
                    serverStarted,
                    serverHandled,
                    serverStreamMessagesReceived,
                    serverStreamMessagesSent,
                    serverHandledLatencySeconds,
                    currentThreadCount);
        }

       public <R, S> ServerMetrics createMetricsForZebraMethod(MethodDescriptor<R, S> methodDescriptor) {
            return new ServerMetrics(
                    GrpcMethod.ofZebraMethod(methodDescriptor),
                    serverStarted,
                    serverHandled,
                    serverStreamMessagesReceived,
                    serverStreamMessagesSent,
                    serverHandledLatencySeconds,
                    currentThreadCount);
        }


        public ServerMetrics createMetricsForGWMethod(String serviceName, String serviceMethod, MethodDescriptor.MethodType methodType) {
            return new ServerMetrics(
                    new GrpcMethod(serviceName, serviceMethod, methodType),
                    serverStarted,
                    serverHandled,
                    serverStreamMessagesReceived,
                    serverStreamMessagesSent,
                    serverHandledLatencySeconds,
                    currentThreadCount);
        }
    }
}
