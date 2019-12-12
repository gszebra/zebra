package com.guosen.zebra.core.grpc.client.unary;

import java.io.InputStream;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Message;
import com.guosen.zebra.core.exception.RpcErrorMsgConstant;
import com.guosen.zebra.core.exception.RpcServiceException;
import com.guosen.zebra.core.message.json.JsonReply;

import io.grpc.MethodDescriptor;

public class GrpcBlockingUnaryCommand extends GrpcHystrixCommand {

	private static final Logger log = LogManager.getLogger(GrpcBlockingUnaryCommand.class);

	public GrpcBlockingUnaryCommand(String serviceName, String methodName, Boolean isEnabledFallBack) {
		super(serviceName, methodName, isEnabledFallBack);
	}

	@Override
	protected Message run0(Message req, MethodDescriptor<Message, Message> methodDesc, Integer timeOut,
			GrpcUnaryClientCall clientCall, Class<?> responseType) {
		try {
			return clientCall.blockingUnaryResult(req, methodDesc);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			RpcServiceException rpcService = new RpcServiceException(e, RpcErrorMsgConstant.BIZ_DEFAULT_EXCEPTION);
			throw rpcService;
		}
	}

	@Override
	protected JSONObject runJs(Message req, MethodDescriptor<Message, Message> methodDesc, Integer timeOut,
			GrpcUnaryClientCall clientCall) {
		try {
			JsonReply reply = (JsonReply) clientCall.blockingUnaryResult(req, methodDesc);
			return JSON.parseObject(reply.getMessage());
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
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
			InputStream reply = (InputStream) clientCall.blockingUnaryIsResult(req, methodDesc);
			return reply;
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
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
