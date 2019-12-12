package com.guosen.zebra.core.boot.runner;

import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;

import com.guosen.zebra.core.boot.autoconfigure.GrpcProperties;
import com.guosen.zebra.core.grpc.RpcServiceBaseInfo;
import com.guosen.zebra.core.server.RpcServiceExport;


/** 
* @ClassName: GrpcServiceRunner 
* @Description: spring boot 启动，扫描service对GRPC服务进行注册
* @author 邓启翔 
* @date 2017年10月30日 下午4:41:31 
*  
*/
@Order(1)
public class GrpcServiceRunner implements DisposableBean, CommandLineRunner {
	@Autowired
	private AbstractApplicationContext applicationContext;

	@Autowired
	private GrpcProperties grpcProperties;

	@Autowired
	private RpcSeriviceBaseInfoReader rpcSeriviceBaseInfoReader;
	
	private RpcServiceExport server;

	@Override
	public void run(String... args) {
		System.err.println(">>>>>>>>>>>>>>>服务启动执行，对ZebraService进行注册中心注册<<<<<<<<<<<<<");

		List<RpcServiceBaseInfo> rpcSeviceList= rpcSeriviceBaseInfoReader.getRpcSeriviceBaseInfos();
		if (CollectionUtils.isEmpty(rpcSeviceList)) {
			System.err.println(">>>>>>>>>>>>>>>没有Grpc服务定义<<<<<<<<<<<<<");
			return;
		}
		server = new RpcServiceExport();
		server.export(rpcSeviceList,grpcProperties,applicationContext);
		System.err.println(">>>>>>>>>>>>>>>Grpc服务启动成功<<<<<<<<<<<<<");
	}

	@Override
	public void destroy() {
		server.destroy();
	}
}
