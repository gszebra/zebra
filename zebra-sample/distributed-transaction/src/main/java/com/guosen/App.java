package com.guosen;

import com.guosen.zebra.ZebraRun;
import com.guosen.zebra.core.grpc.anotation.ZebraConf;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ZebraConf(confName="com.guosen.zebra.distributed.transaction.cfg")
public class App {

    public static void main(String[] args) throws Exception {
        ZebraRun.run(args, App.class, true);
    }
}
