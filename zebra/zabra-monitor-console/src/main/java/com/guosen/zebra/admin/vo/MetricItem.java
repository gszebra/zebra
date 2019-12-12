package com.guosen.zebra.admin.vo;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class MetricItem {
//    private String gRpcType = "";
    private String gRpcService = "";
    private String gRpcMethod = "";
    private String instance = "";
    private Float qps;
    private Integer avgLatency;
    private String status;
    private Integer port;

    public static final String STAT_DOWN = "DOWN";
    public static final String STAT_READY = "READY";
    public static final String STAT_SERVING = "SERVING";

    public MetricItem() {
        this.status = STAT_READY;
    }

    public MetricItem(String prometheusLabelStr,int port) {
        buildByLabelStr(prometheusLabelStr);
        this.port = port;
    }

    @Override
    public String toString() {
        return "MetricItem{" +
                "gRpcService='" + gRpcService + '\'' +
                ", gRpcMethod='" + gRpcMethod + '\'' +
                ", instance='" + instance + '\'' +
                ", qps=" + qps +
                ", avgLatency=" + avgLatency +
                ", status='" + status + '\'' +
                '}';
    }

    public void buildByLabelStr(String prometheusLabelStr) {
        if (StringUtils.isNotEmpty(prometheusLabelStr)) {
            this.status = "SERVING";
            Arrays.stream(prometheusLabelStr.split(",")).forEach((s) -> {
                String[] arr = s.split("=");
                if (null != arr && 2 == arr.length) {
                    String k = arr[0];
                    String v = StringUtils.replace(arr[1], "\"", "");
                    switch (k) {
//                        case "grpc_type":
//                            this.gRpcType = v;
//                            break;
                        case "grpc_service":
                            this.gRpcService = v;
                            break;
                        case "grpc_method":
                            this.gRpcMethod = v;
                            break;
                        case "instance":
                            this.instance = v;
                            break;

                    }

                }
            });
        }
    }



//    public String getgRpcType() {
//        return gRpcType;
//    }
//
//    public void setgRpcType(String gRpcType) {
//        this.gRpcType = gRpcType;
//    }

    public String getgRpcService() {
        return gRpcService;
    }

    public void setgRpcService(String gRpcService) {
        this.gRpcService = gRpcService;
    }

    public String getgRpcMethod() {
        return gRpcMethod;
    }

    public void setgRpcMethod(String gRpcMethod) {
        this.gRpcMethod = gRpcMethod;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public Float getQps() {
        return qps;
    }

    public void setQps(Float qps) {
        this.qps = Float.valueOf(Math.round(qps.floatValue() * 10) / 10.0f);
    }

    public Integer getAvgLatency() {
        return avgLatency;
    }

    public void setAvgLatency(Integer avgLatency) {
        this.avgLatency = avgLatency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

}
