package com.guosen.zebra.core.grpc.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

public class GrpcStubServerExporter implements GrpcProtocolExporter {
	private static final Logger log =LogManager.getLogger(GrpcStubServerExporter.class);
    @Override
    public ServerServiceDefinition export(Class<?> protocol, Object protocolImpl) {
        Object obj = protocolImpl;
        if (!(obj instanceof BindableService)) {
            throw new IllegalStateException(" Object is not io.grpc.BindableService,can not export " + obj);
        } else {
            BindableService bindableService = (BindableService) obj;
            log.info("'{}' service has been registered.", bindableService.getClass().getName());
            return bindableService.bindService();
        }
    }
}
