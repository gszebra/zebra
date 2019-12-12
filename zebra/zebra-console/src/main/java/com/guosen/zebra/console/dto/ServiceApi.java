package com.guosen.zebra.console.dto;

import com.google.common.collect.Lists;
import java.util.List;

public class ServiceApi
{
  private List<String> services = Lists.newArrayList();
  private String proto;
  private List<String> methods = Lists.newArrayList();
  private List<String> ips = Lists.newArrayList();
  
  public String getProto()
  {
    return this.proto;
  }
  
  public void setProto(String proto)
  {
    this.proto = proto;
  }
  
  public List<String> getMethods()
  {
    return this.methods;
  }
  
  public void setMethods(List<String> methods)
  {
    this.methods = methods;
  }
  
  public List<String> getServices()
  {
    return this.services;
  }
  
  public void setServices(List<String> services)
  {
    this.services = services;
  }
  
  public List<String> getIps()
  {
    return this.ips;
  }
  
  public void setIps(List<String> ips)
  {
    this.ips = ips;
  }
}
