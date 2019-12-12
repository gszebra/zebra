package com.guosen.zebra.core.registry.etcd;

import com.coreos.jetcd.watch.WatchEvent;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;

public interface MonitorNotifyListener {

  public interface NotifyServiceListener {
    void notify(RpcServiceBaseInfo serviceInfo, WatchEvent event);
  }
}
