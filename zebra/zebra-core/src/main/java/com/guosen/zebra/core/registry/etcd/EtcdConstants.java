package com.guosen.zebra.core.registry.etcd;
/** 
* @ClassName: EtcdConstants 
* @Description: ETCD3常量类
* @author 邓启翔 
* @date 2017年10月30日 下午3:39:47 
*  
*/
public interface EtcdConstants {

    /**
     * service 最长存活周期（Time To Live），单位秒。 每个service会注册一个ttl类型的check，在最长TTL秒不发送心跳 就会将service变为不可用状态。
     */
    public static int  TTL                       = 10;
   
}
