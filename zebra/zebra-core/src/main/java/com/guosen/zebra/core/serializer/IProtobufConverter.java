package com.guosen.zebra.core.serializer;

import com.guosen.zebra.core.exception.ProtobufAnnotationException;

public interface IProtobufConverter {

    Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException;

    Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException;
}
