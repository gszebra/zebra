package com.guosen.zebra.core.registry.etcd;

import java.util.List;

import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;

public interface NotifyListener {

  public interface NotifyServiceListener {
    void notify(RpcServiceBaseInfo serviceInfo, List<String> urls);
  }
}
