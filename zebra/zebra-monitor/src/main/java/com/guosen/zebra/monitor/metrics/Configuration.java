package com.guosen.zebra.monitor.metrics;

import io.prometheus.client.CollectorRegistry;

/**
 * Holds information about which metrics should be kept track of during rpc calls. Can be used to
 * turn on more elaborate and expensive metrics, such as latency histograms.
 */
public class Configuration {
	private static double[] DEFAULT_LATENCY_BUCKETS = new double[] {10, 100, 500, 1000, 2000, 5000};

	private final boolean isIncludeLatencyHistograms;
	private final CollectorRegistry collectorRegistry;
	private final double[] latencyBuckets;

	private Configuration(boolean isIncludeLatencyHistograms, CollectorRegistry collectorRegistry,
			double[] latencyBuckets) {
		this.isIncludeLatencyHistograms = isIncludeLatencyHistograms;
		this.collectorRegistry = collectorRegistry;
		this.latencyBuckets = latencyBuckets;
	}

	/**
	 * Returns a {@link Configuration} for recording all cheap metrics about the
	 * rpcs.
	 */
	public static Configuration simpleMetrics() {
		return new Configuration(false /* isIncludeLatencyHistograms */, CollectorRegistry.defaultRegistry,
				DEFAULT_LATENCY_BUCKETS);
	}

	/**
	 * Returns a {@link Configuration} for recording all metrics about the rpcs.
	 * This includes metrics which might produce a lot of data, such as latency
	 * histograms.
	 */
	public static Configuration allMetrics() {
		return new Configuration(true /* isIncludeLatencyHistograms */, CollectorRegistry.defaultRegistry,
				DEFAULT_LATENCY_BUCKETS);
	}

	/**
	 * Returns a copy {@link Configuration} with the difference that Prometheus
	 * metrics are recorded using the supplied {@link CollectorRegistry}.
	 */
	public Configuration withCollectorRegistry(CollectorRegistry collectorRegistry) {
		return new Configuration(isIncludeLatencyHistograms, collectorRegistry, latencyBuckets);
	}

	/**
	 * Returns a copy {@link Configuration} with the difference that the latency
	 * histogram values are recorded with the specified set of buckets.
	 */
	public Configuration withLatencyBuckets(double[] buckets) {
		return new Configuration(isIncludeLatencyHistograms, collectorRegistry, buckets);
	}

	/**
	 * Returns whether or not latency histograms for calls should be included.
	 */
	public boolean isIncludeLatencyHistograms() {
		return isIncludeLatencyHistograms;
	}

	/**
	 * Returns the {@link CollectorRegistry} used to record stats.
	 */
	public CollectorRegistry getCollectorRegistry() {
		return collectorRegistry;
	}

	/**
	 * Returns the histogram buckets to use for latency metrics.
	 */
	public double[] getLatencyBuckets() {
		return latencyBuckets;
	}
}
