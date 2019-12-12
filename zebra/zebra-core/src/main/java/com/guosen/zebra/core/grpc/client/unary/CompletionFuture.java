package com.guosen.zebra.core.grpc.client.unary;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * @ClassName: CompletionFuture
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2017年12月1日 上午10:28:09 Futures.addCallback(future, new FutureCallback
 *       <HelloReply>() {
 * @param <T>
 */
public class CompletionFuture<T> extends CompletableFuture<T> {

	private SocketAddress remote;

	public SocketAddress getRemote() {
		return remote;
	}

	public void setRemote(SocketAddress remote) {
		this.remote = remote;
	}

}
