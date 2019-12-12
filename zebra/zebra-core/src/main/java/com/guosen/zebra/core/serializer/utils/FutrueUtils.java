/**   
* @Title: FutrueUtils.java 
* @Package com.guosen.zebra.core.serializer.utils 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年12月15日 上午8:11:40 
* @version V1.0   
*/
package com.guosen.zebra.core.serializer.utils;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.protobuf.Message;
import com.guosen.zebra.core.common.FutureType;
import com.guosen.zebra.core.common.RpcContext;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.exception.ProtobufException;
import com.guosen.zebra.core.exception.RpcFrameworkException;
import com.guosen.zebra.core.grpc.client.GrpcCallOptions;
import com.guosen.zebra.core.grpc.client.GrpcResponse;
import com.guosen.zebra.core.grpc.client.unary.CompletionFuture;
import com.guosen.zebra.core.message.json.JsonReply;

/**
 * @ClassName: FutrueUtils
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2017年12月15日 上午8:11:40
 * 
 */
public class FutrueUtils {
	private static final Logger log = LogManager.getLogger(FutrueUtils.class);
	private static ScheduledExecutorService ses = Executors
			.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1);

	public static <T> CompletableFuture<Object> makeCompletableFuture(CompletionFuture<T> lisfuture, FutureType type,
			Class<?> responseType, int timeout) {
		final List<SocketAddress> remote = Lists.newArrayList();
		//schedule watcher
		CompletableFuture<Object> futrue = new CompletableFuture<Object>();
		ses.schedule(() -> {
	        if (!lisfuture.isDone()) {
	        	lisfuture.completeExceptionally(new TimeoutException("Timeout after " + timeout));
	        }

	    }, timeout, TimeUnit.MILLISECONDS);
		
		lisfuture.whenComplete((v,t)->{
			if(t!=null){
				futrue.completeExceptionally(t);
				return;
			}
			try {
				if (ZebraConstants.IS_RECORD_TARGET_IP)
					remote.add(((CompletionFuture<?>) lisfuture).getRemote());
				switch (type) {
				case ISNOMARL:
					Message response = (Message) v;
					futrue.complete(transformMessage(response, responseType));
					break;
				case ISJSON:
					JsonReply reply = (JsonReply) v;
					futrue.complete(JSON.parseObject(reply.getMessage()));
					break;
				case ISINPUTSTREAM:
					futrue.complete(v);
					break;
				default:
					futrue.completeExceptionally(new IllegalArgumentException("Unkonwn future type"));
				}
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				futrue.completeExceptionally(e);
			}
		});

		if (ZebraConstants.IS_RECORD_TARGET_IP)
			RpcContext.getContext().set(GrpcCallOptions.GRPC_CURRENT_ADDR_KEY, remote);
		return futrue;
	}

	private static Object transformMessage(Message message, Class<?> responseType) {
		GrpcResponse response = new GrpcResponse.Default(message, responseType);
		try {
			return response.getResponseArg();
		} catch (ProtobufException e) {
			RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
			throw rpcFramwork;
		}
	}
}
