package com.guosen;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.guosen.zebra.ZebraRun;
import com.guosen.zebra.core.grpc.anotation.ZebraConf;

@SpringBootApplication
@ZebraConf(confName="com.guosen.zebra.sample.sharding.CreditService")
public class App {
	public static void main(String[] args) throws Exception {
		ZebraRun.run(args, App.class,true);
	}
}
