package com.guosen.zebra.core.grpc.client;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.serializer.utils.ReflectUtils;

import io.grpc.Channel;

public class GrpcStubClient<AbstractStub> implements GrpcProtocolClient<AbstractStub> {

  private final Class<? extends AbstractStub> stubClass;

  private final Map<String, Object> params;

  public GrpcStubClient(Class<? extends AbstractStub> stubClass, Map<String, Object> params) {
    this.stubClass = stubClass;
    this.params = params;
  }

  public String getStubClassName() {
    return this.stubClass.getName();
  }

  @SuppressWarnings("unchecked")
  @Override
  public AbstractStub getGrpcClient(ChannelCall channelPool, int callType, int callTimeout) {
    String stubClassName = GrpcStubClient.this.getStubClassName();
    Channel channel = null;
    if (StringUtils.contains(stubClassName, "$")) {
      try {
        String parentName = StringUtils.substringBefore(stubClassName, "$");
        Class<?> clzz = ReflectUtils.name2class(parentName);
        Method method;
        switch (callType) {
          case ZebraConstants.RPCTYPE_ASYNC:
            method = clzz.getMethod("newFutureStub", io.grpc.Channel.class);
            break;
          case ZebraConstants.RPCTYPE_BLOCKING:
            method = clzz.getMethod("newBlockingStub", io.grpc.Channel.class);
            break;
          default:
            method = clzz.getMethod("newFutureStub", io.grpc.Channel.class);
            break;
        }
        channel = channelPool.borrowChannel(params,(String) params.get(ZebraConstants.SERVICE_NAME));
        AbstractStub stubInstance = (AbstractStub) method.invoke(null, channel);
        return stubInstance;
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "stub definition not correct，do not edit proto generate file", e);
      } finally {
        if (channel != null) {
          channelPool.returnChannel(params,(String) params.get(ZebraConstants.SERVICE_NAME), channel);
        }
      }
    } else {
      throw new IllegalArgumentException(
          "stub definition not correct，do not edit proto generate file");
    }
  }
}
