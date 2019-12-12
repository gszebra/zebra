/**   
* @Title: ServerTestListener.java 
* @Package com.guosen.server.demo 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年11月17日 下午1:59:36 
* @version V1.0   
*/
package com.guosen.zebra.core.grpc.server.json;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.guosen.zebra.core.message.json.JsonRequest;
import com.guosen.zebra.core.serializer.utils.JReflectionUtils;

import io.grpc.ForwardingServerCallListener;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.Status;

/** 
* @ClassName: ServerTestListener 
* @Description: 代理json请求
* @author 邓启翔 
* @date 2017年11月17日 下午1:59:36 
*  
*/
public class ServerJsonListener<ReqT,RespT> extends ForwardingServerCallListener<ReqT>{
	private static final Logger log = LogManager.getLogger(ServerJsonListener.class);
	private final ServerCall.Listener<ReqT> delegate;
	private final ServerCall<ReqT, RespT> call;
	public ServerJsonListener(ServerCall.Listener<ReqT> delegate,ServerCall<ReqT, RespT> call) {
	      this.delegate = delegate;
	      this.call=call;
	}
	@Override
	protected Listener<ReqT> delegate() {
		// TODO Auto-generated method stub
		return delegate;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(ReqT message) {
		Message.Builder builder = null;
		Class<?> t = JsonServerServiceInterceptors.reqClzs.get(call.getMethodDescriptor().getFullMethodName());
		try {
			builder = (Message.Builder) JReflectionUtils.runStaticMethod(t, "newBuilder");
			JsonFormat.parser().ignoringUnknownFields().merge(((JsonRequest) message).getMessage(), builder);
			delegate.onMessage((ReqT) builder.build());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException();
		}
	}
	
	@Override
    public void onHalfClose() {
        super.onHalfClose();
    }

    @Override
    public void onCancel() {
        super.onCancel();
    }

    @Override
    public void onComplete() {
        super.onComplete();
    }

    @Override
    public void onReady() {
        super.onReady();
    }
}
