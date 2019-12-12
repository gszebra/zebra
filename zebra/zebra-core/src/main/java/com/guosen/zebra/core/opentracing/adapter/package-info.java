/**
 * Zebra框架使用zipkin2进行tracing，但是引入的相关第三件有些只支持opentracing的API，需要我们做下适配。<br/>
 * 此包实现OpenTracing的相关接口，最后将发送的动作委托到zipkin2的客户端<br/>
 * 当前只适配了少部分接口，以便给Sharding-JDBC使用，其他模块不要使用此包下面的tracer。
 */
package com.guosen.zebra.core.opentracing.adapter;