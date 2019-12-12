package com.guosen.zebra.core.opentracing;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.guosen.zebra.core.grpc.util.PropertiesContent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

public final class TraceAgent {
	private static final Logger LOGGER = LogManager.getLogger(TraceAgent.class);

	private static final String TRACER_ADDRESS_KEY = "opentracing.upd.addr";

	private static final String HTTP_PREFIX = "http://";

	private static final Supplier<TraceAgent> INSTANCE = Suppliers.memoize(TraceAgent::init);

	private AsyncReporter<zipkin2.Span> report = null;

	private TraceAgent(){}

	private static TraceAgent init() {
		TraceAgent traceAgent = new TraceAgent();

		String tracerAddress = PropertiesContent.getStrValue(TRACER_ADDRESS_KEY, null);
		if (StringUtils.isBlank(tracerAddress)) {
			LOGGER.error("Could not get tracer address, trace agent will not send trace report.");
			return traceAgent;
		}

		OkHttpSender sender = OkHttpSender.create(HTTP_PREFIX + tracerAddress);
		traceAgent.report = AsyncReporter.builder(sender).build();

		return traceAgent;
	}

	public static TraceAgent getInstance() {
		return INSTANCE.get();
	}
	
	public void send(final Span span) {
		if (report == null) {
			return;
		}

		try {
			report.report(span);
			report.flush();
		} catch(Exception e) {
			LOGGER.error("report zipkin error :" + e.getMessage(),e);
		}
	}
}
