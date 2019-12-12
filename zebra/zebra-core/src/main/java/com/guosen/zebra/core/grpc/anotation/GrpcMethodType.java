package com.guosen.zebra.core.grpc.anotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface GrpcMethodType {

  io.grpc.MethodDescriptor.MethodType methodType() default io.grpc.MethodDescriptor.MethodType.UNARY;

  Class<?> requestType() default void.class;

  Class<?> responseType() default void.class;
}
