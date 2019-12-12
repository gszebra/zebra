package com.guosen.zebra.monitor.metrics.exporter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class CompactPrometheusTextFormat extends TextFormat {
    public CompactPrometheusTextFormat() {
        super();
    }

    public static void write004(Writer writer, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
        while (mfs.hasMoreElements()) {
            Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
            for (Collector.MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
                writer.write(sample.name);
                if (sample.labelNames.size() > 0) {
                    writer.write('{');
                    for (int i = 0; i < sample.labelNames.size(); ++i) {
                        writer.write(sample.labelNames.get(i));
                        writer.write("=\"");
                        writeEscapedLabelValue(writer, sample.labelValues.get(i));
                        writer.write("\",");
                    }
                    writer.write('}');
                }
                writer.write(' ');
                writer.write(Collector.doubleToGoString(sample.value));
                writer.write('\n');
            }
        }
    }

    public static String getContentType() {
        return TextFormat.CONTENT_TYPE_004;
    }

    private static final CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    public static String getMetricsString() {
        StringBuilder sb = new StringBuilder();
        Enumeration<Collector.MetricFamilySamples> mfs = registry.filteredMetricFamilySamples(Collections.emptySet());
        while(mfs.hasMoreElements()) {
            Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
            if(metricFamilySamples.samples.size() ==0) continue;
            sb.append("# HELP ").append(metricFamilySamples.name).append(" ").append(metricFamilySamples.help).append("\n");
            sb.append("# TYPE ").append(metricFamilySamples.name).append(" ").append(metricFamilySamples.type.toString().toLowerCase()).append("\n");
            for (Collector.MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
                sb.append(sample.name);
                if (sample.labelNames.size() > 0) {
                    sb.append('{');
                    for (int i = 0; i < sample.labelNames.size(); ++i) {
                        sb.append(sample.labelNames.get(i));
                        sb.append("=\"");
                        sb.append(escapeLabel(sample.labelValues.get(i)));
                        sb.append("\"");
                        if(i<sample.labelNames.size() -1){
                        	sb.append(",");
                        }
                    }
                    sb.append('}');

                }
                sb.append(' ');
                sb.append(Collector.doubleToGoString(sample.value));
                sb.append("\n");
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }

    public static List<String> getMetricsList() {
        List<String> metrics = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        Enumeration<Collector.MetricFamilySamples> mfs = registry.filteredMetricFamilySamples(Collections.emptySet());
        while(mfs.hasMoreElements()) {
            Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
            for (Collector.MetricFamilySamples.Sample sample : metricFamilySamples.samples) {

                sb.append(sample.name);
                if (sample.labelNames.size() > 0) {
                    sb.append('{');
                    for (int i = 0; i < sample.labelNames.size(); ++i) {
                        sb.append(sample.labelNames.get(i));
                        sb.append("=\"");
                        sb.append(escapeLabel(sample.labelValues.get(i)));
                        sb.append("\",");
                    }
                    sb.append('}');

                }
                sb.append(' ');
                sb.append(Collector.doubleToGoString(sample.value));
                //sb.append('\n');
                metrics.add(sb.toString());
                sb.delete(0, sb.length());
            }
        }
        return metrics;
    }

    private static String escapeLabel(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void writeEscapedLabelValue(Writer writer, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    writer.append("\\\\");
                    break;
                case '\"':
                    writer.append("\\\"");
                    break;
                case '\n':
                    writer.append("\\n");
                    break;
                default:
                    writer.append(c);
            }
        }
    }

}
