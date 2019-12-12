package com.guosen.zebra.core.grpc.client.unary;

import java.io.InputStream;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Message;
import com.guosen.zebra.core.common.FutureType;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.exception.RpcErrorMsgConstant;
import com.guosen.zebra.core.exception.RpcServiceException;
import com.guosen.zebra.core.serializer.utils.FutrueUtils;

import io.grpc.MethodDescriptor;

public class GrpcFutureUnaryCommand extends GrpcHystrixCommand {

	private static final Logger logger = LogManager.getLogger(GrpcBlockingUnaryCommand.class);

	public GrpcFutureUnaryCommand(String serviceName, String methodName, Boolean isEnabledFallBack) {
		super(serviceName, methodName, isEnabledFallBack);
	}

	@Override
	protected Message run0(Message req, MethodDescriptor<Message, Message> methodDesc, Integer timeOut,
			GrpcUnaryClientCall clientCall, Class<?> responseType) {
		try {
			CompletionFuture<? extends Message> future = clientCall.unaryFuture(req, methodDesc);
			RpcContext.getContext()
					.setFuture(FutrueUtils.makeCompletableFuture(future, FutureType.ISNOMARL, responseType, timeOut));
			return null;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			if (e instanceof TimeoutException) {
				RpcServiceException rpcService = new RpcServiceException(e, RpcErrorMsgConstant.SERVICE_TIMEOUT);
				throw rpcService;
			} else {
				RpcServiceException rpcService = new RpcServiceException(e, RpcErrorMsgConstant.BIZ_DEFAULT_EXCEPTION);
				throw rpcService;
			}
		}
	}

	@Override
	protected JSONObject runJs(Message req, MethodDescriptor<Message, Message> methodDesc, Integer timeOut,
			GrpcUnaryClientCall clientCall) {
		try {
			CompletionFuture<? extends Message> future = clientCall.unaryFuture(req, methodDesc);
			RpcContext.getContext()
					.setFuture(FutrueUtils.makeCompletableFuture(future, FutureType.ISJSON, null, timeOut));
			return null;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			if (e instanceof TimeoutException) {
				RpcServiceException rpcService = new RpcServiceException(e, RpcErrorMsgConstant.SERVICE_TIMEOUT);
				throw rpcService;
			} else {
				RpcServiceException rpcService = new RpcServiceException(e, RpcErrorMsgConstant.BIZ_DEFAULT_EXCEPTION);
				throw rpcService;
			}
		}
	}

	@Override
	protected InputStream runIs(InputStream req, MethodDescriptor<InputStream, InputStream> methodDesc, Integer timeOut,
			GrpcUnaryClientCall clientCall) {
		try {
			CompletionFuture<InputStream> future = clientCall.unaryIsFuture(req, methodDesc);
			RpcContext.getContext()
					.setFuture(FutrueUtils.makeCompletableFuture(future, FutureType.ISINPUTSTREAM, null, timeOut));
			return null;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			if (e instanceof TimeoutException) {
				RpcServiceException rpcService = new RpcServiceException(e, RpcErrorMsgConstant.SERVICE_TIMEOUT);
				throw rpcService;
			} else {
				RpcServiceException rpcService = new RpcServiceException(e, RpcErrorMsgConstant.BIZ_DEFAULT_EXCEPTION);
				throw rpcService;
			}
		}
	}

}
