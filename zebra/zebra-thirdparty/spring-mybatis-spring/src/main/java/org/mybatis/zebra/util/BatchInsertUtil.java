/**
 * Copyright 2010-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.zebra.util;

import org.mybatis.zebra.annotation.BatchInsert;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 批量插入注解工具类
 */
public class BatchInsertUtil {

  /**
   * 方法是带有BatchInsert注解信息缓存
   */
  private static final Map<String, Boolean> METHOD_BATCH_INFO_CACHE = new ConcurrentHashMap<>();

  /**
   * 点号
   */
  private static final char DOT = '.';

  /**
   * 判断方法是否带有BatchInsert注解
   * 
   * @param fullMethodName
   *          方法全称格式为 packageName.className.methodName
   * @return true : 方法带有BatchInsert注解; false ；方法不带BatchInsert注解
   */
  public static boolean isBatchInsertMethod(String fullMethodName) {
    // 必须使用缓存，否则每个sql执行过来都调用一次，性能会极差！
    return METHOD_BATCH_INFO_CACHE.computeIfAbsent(fullMethodName, BatchInsertUtil::doCheckIsBatchInsertMethod);
  }

  private static boolean doCheckIsBatchInsertMethod(String fullMethodName) {
    int lastDotIndex = fullMethodName.lastIndexOf(DOT);
    String clzName = fullMethodName.substring(0, lastDotIndex);
    String methodName = fullMethodName.substring(lastDotIndex + 1);

    try {
      Class clz = Class.forName(clzName);
      Method method = clz.getMethod(methodName, List.class);
      return method.getAnnotation(BatchInsert.class) != null;
    } catch (NoSuchMethodException e) {
      return false;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
