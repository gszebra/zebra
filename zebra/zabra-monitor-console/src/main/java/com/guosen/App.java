package com.guosen;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.guosen.zebra.ZebraRun;
import com.guosen.zebra.core.grpc.anotation.ZebraConf;

@SpringBootApplication
@ZebraConf(confName="zebra.monitor")
@EnableScheduling
public class App{
    public static void main(String[] args) throws Exception {
    	ZebraRun.run(args, App.class, true);
    }
}
