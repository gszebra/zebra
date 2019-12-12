
package com.guosen.zebra.core.grpc.server.json;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.TypeRegistry;
import com.guosen.zebra.core.message.json.JsonReply;
import com.guosen.zebra.core.serializer.help.Pojo2ProtobufHelp;

import io.grpc.Attributes;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.Status;

/** 
* @ClassName: ZebraForwardingServerCall 
* @Description: 代理server返回JSON处理
* @author 邓启翔 
* @date 2017年11月20日 上午9:38:36 
* 
* @param <ReqT>
* @param <RespT> 
*/
public class ZebraJsonForwardingServerCall<ReqT, RespT> extends ServerCall<ReqT, RespT> {
	private static final Logger log = LogManager.getLogger(ZebraJsonForwardingServerCall.class);
	private final ServerCall<RespT, RespT> call;
	
	public ZebraJsonForwardingServerCall(ServerCall<RespT, RespT> call) {
		this.call = call;
	}

	protected ServerCall<RespT, RespT> delegate() {
		return call;
	}

	@Override
	public void request(int numMessages) {
		delegate().request(numMessages);
	}

	@Override
	public void sendHeaders(Metadata headers) {
		delegate().sendHeaders(headers);
	}

	@Override
	public boolean isReady() {
		return delegate().isReady();
	}

	@Override
	public void close(Status status, Metadata trailers) {
		delegate().close(status, trailers);
	}

	@Override
	public boolean isCancelled() {
		return delegate().isCancelled();
	}

	@Override
	public void setMessageCompression(boolean enabled) {
		delegate().setMessageCompression(enabled);
	}

	@Override
	public void setCompression(String compressor) {
		delegate().setCompression(compressor);
	}

	@Override
	public Attributes getAttributes() {
		return delegate().getAttributes();
	}

	@SuppressWarnings("unchecked")
	@Override
    public void sendMessage(RespT message) {
		String jsonFormat;
		JsonReply rep;
		try {
			if(message!=null){
				if (Pojo2ProtobufHelp.anyType.size() == 0) {
					jsonFormat = JsonFormat.printer().includingDefaultValueFields().preservingProtoFieldNames()
							.print((MessageOrBuilder) message);
				}else{
					TypeRegistry.Builder build = JsonFormat.TypeRegistry.newBuilder();
					Pojo2ProtobufHelp.anyType.forEach(desc ->{
						build.add(desc);
					});
					JsonFormat.Printer printer = JsonFormat.printer().includingDefaultValueFields().preservingProtoFieldNames().usingTypeRegistry(build.build());
					jsonFormat =printer.print((MessageOrBuilder) message);
				}
				if(jsonFormat.contains("repeatJson")){//支持repeat map
					JSONObject json = JSON.parseObject(jsonFormat);
					JSONObject repeat = json.getJSONObject("repeatJson");
					for (Map.Entry<String, Object> entry: repeat.entrySet()) {
						String key = entry.getKey();
						JSONArray val = (JSONArray) entry.getValue();
						json.put(key, val);
					}
					json.remove("repeatJson");
					jsonFormat = json.toJSONString();
				}
				rep = JsonReply.newBuilder().setMessage(jsonFormat).build();
				log.debug("begin send json response ={}",jsonFormat);
				delegate().sendMessage((RespT) rep);
			}else{
				delegate().sendMessage(message);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			throw Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException();
		}
    }

	@SuppressWarnings("unchecked")
	@Override
	public MethodDescriptor<ReqT, RespT> getMethodDescriptor() {
		return (MethodDescriptor<ReqT, RespT>) delegate().getMethodDescriptor();
	}
}
