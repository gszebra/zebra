package com.guosen.zebra.sample.start.svc1.service.impl;


import com.guosen.zebra.core.grpc.anotation.ZebraService;
import com.guosen.zebra.sample.start.svc1.model.hello.HelloReply;
import com.guosen.zebra.sample.start.svc1.model.hello.HelloRequest;
import com.guosen.zebra.sample.start.svc1.service.HelloService;

@ZebraService
public class HelloServiceImpl implements HelloService {

    @Override
    public HelloReply sayHello(HelloRequest hellorequest) {
        long currentTime = System.currentTimeMillis();
        String message = String.format("Hi, %s From svc1, time : %d", hellorequest.getName(), currentTime);

        HelloReply helloReply = new HelloReply();
        helloReply.setMessage(message);

        return helloReply;
    }
}
