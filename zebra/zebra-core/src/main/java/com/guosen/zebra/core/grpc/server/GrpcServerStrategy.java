package com.guosen.zebra.core.grpc.server;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;
import com.guosen.zebra.core.grpc.server.json.JsonServerServiceInterceptors;
import com.guosen.zebra.core.serializer.utils.ReflectUtils;
import com.guosen.zebra.monitor.metrics.Configuration;
import com.guosen.zebra.monitor.metrics.server.MonitoringGatewayCall;

import io.grpc.BindableService;
import io.grpc.MethodDescriptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;

public class GrpcServerStrategy {
    private final GrpcProtocolExporter exporter;

    private final Class<?>             protocolClass;

    private final Object               protocolImpl;
    

    public GrpcServerStrategy(RpcServiceBaseInfo seviceConf, Object protocolImpl){
        if (protocolImpl instanceof BindableService) {
            this.exporter = new GrpcStubServerExporter();
            this.protocolClass = protocolImpl.getClass();
        } else {
            Class<?> protocol;
            try {
                protocol = ReflectUtils.name2class(seviceConf.getService());
                if (!protocol.isAssignableFrom(protocolImpl.getClass())) {
                    throw new IllegalStateException("protocolClass " + seviceConf.getService()
                                                    + " is not implemented by protocolImpl which is of class "
                                                    + protocolImpl.getClass());
                }
            } catch (ClassNotFoundException e) {
                protocol = protocolImpl.getClass();
            }
            this.protocolClass = protocol;
            this.exporter = new DefaultProxyExporter(seviceConf);
        }
        this.protocolImpl = protocolImpl;
    }

    public List<ServerServiceDefinition> getServerDefintion() throws IllegalArgumentException, IllegalAccessException {
    	ServerServiceDefinition dfDefinition = exporter.export(protocolClass, protocolImpl);//默认定义
    	Collection<ServerMethodDefinition<?, ?>> methods = dfDefinition.getMethods();
    	methods.stream().forEach(item->{//初始化监控
    		String fullMehodName =item.getMethodDescriptor().getFullMethodName();
    		String serviceName = fullMehodName.split("/")[0];
    		String methodName = fullMehodName.split("/")[1];
    		MonitoringGatewayCall call = new MonitoringGatewayCall(serviceName, methodName, MethodDescriptor.MethodType.UNARY,
                    Configuration.allMetrics());
    		call.close(Status.OK);
    	});
    	ServerServiceDefinition isDefinition = ServerInterceptors.useInputStreamMessages(dfDefinition);//inputstream定义
    	ServerServiceDefinition jsonDefinition =JsonServerServiceInterceptors.useJsonMessages(dfDefinition);//json定义
    	List<ServerServiceDefinition> defs = Lists.newArrayList();
    	defs.add(dfDefinition);
    	defs.add(isDefinition);
    	defs.add(jsonDefinition);
        return defs;
    }
}
