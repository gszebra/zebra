package com.guosen;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.guosen.zebra.ZebraRun;
import com.guosen.zebra.core.grpc.anotation.ZebraConf;

@SpringBootApplication
@ZebraConf(confName="zebra.console")
@EnableAsync
@EnableCaching
@EnableScheduling
@EnableWebMvc
public class App{
    public static void main(String[] args) throws Exception {
    	ZebraRun.run(args, App.class, true);
    }
}

