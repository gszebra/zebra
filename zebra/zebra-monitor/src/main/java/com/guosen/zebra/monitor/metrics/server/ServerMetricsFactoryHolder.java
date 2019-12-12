package com.guosen.zebra.monitor.metrics.server;

import com.guosen.zebra.monitor.metrics.Configuration;

public class ServerMetricsFactoryHolder {
    public static final ServerMetrics.Factory ALL_METRIC_FACTORY = new ServerMetrics.Factory(Configuration.allMetrics());
}
