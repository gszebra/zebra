package com.guosen.zebra.monitor.metrics.exporter;

import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;

public class HttpExporter {

    @SuppressWarnings("unused")
	private HTTPServer prometheusHttpServer;

    public HttpExporter() throws IOException {
        prometheusHttpServer = new HTTPServer(1234);
    }

    public HttpExporter(int port) throws IOException {
        prometheusHttpServer = new HTTPServer(port);
    }


}
