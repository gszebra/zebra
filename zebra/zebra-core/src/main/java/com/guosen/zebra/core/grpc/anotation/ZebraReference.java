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
@Target({ElementType.FIELD})
public @interface ZebraReference {

  String service() default "";

  String group() default "default";

  String version() default "1.0.0";
  
  String set()  default "0";
  
  int retries() default 0;

  String[] retryMethods() default {};

  boolean async() default false;

  boolean fallback() default false;

  String[] fallBackMethods() default {};

  int timeOut() default 3000;
  
  Class<?>[] interceptors() default {};

  String route()  default "";//调用的ip
}
