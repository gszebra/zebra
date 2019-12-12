package com.guosen.zebra.core.grpc.anotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface ZebraService {

  String service() default "";

  String group() default "default";

  String version() default "1.0.0";
  
  String type() default "server";
  
  String set() default "0";
  
  Class<?>[] interceptors() default {};
}
