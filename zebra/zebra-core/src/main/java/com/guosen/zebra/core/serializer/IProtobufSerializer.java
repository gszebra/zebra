package com.guosen.zebra.core.serializer;

import com.google.protobuf.Message;
import com.guosen.zebra.core.exception.ProtobufException;

public interface IProtobufSerializer {

    Message toProtobuf(Object pojo) throws ProtobufException;

    Object fromProtobuf(Message protoBuf, Class<? extends Object> pojoClazz) throws ProtobufException;
}
