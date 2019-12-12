package com.guosen.zebra.console.strategy;

import com.coreos.jetcd.data.KeyValue;
import com.guosen.zebra.console.dto.ServiceInfo;
import java.util.Map;

public abstract interface AssembleStrategy
{
  public abstract void excute(ServiceInfo paramServiceInfo, KeyValue paramKeyValue, Map<String, ServiceInfo> paramMap);
}
