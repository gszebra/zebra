
package com.guosen.zebra.core.grpc.client;

import java.util.Map;

import io.grpc.Channel;


/** 
* @ClassName: GrpcProtocolClient 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2017年10月31日 下午1:12:35 
* 
* @param <T> 
*/
public interface GrpcProtocolClient<T> {

    public T getGrpcClient(ChannelCall channelCall, int callType, int callTimeout);

    public interface ChannelCall {

        public Channel borrowChannel(Map<String, Object> relParams,String serviceName);

        public void returnChannel(Map<String, Object> relParams,String serviceName, final Channel channel);
    }

}
