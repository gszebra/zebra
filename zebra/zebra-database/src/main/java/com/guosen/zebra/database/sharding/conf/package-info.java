/**
 * 此包下面的实现参考spring-jdbc-spring-boot-starter<br/>
 * 根据zebra微服务框架做了如下对应的调整：
 * 1. 所有sharding配置限制为同一个配置文件（使用配置中心生成的）
 * 2. sharding配置项以zebra.database.shardingcfg开头
 * 3. 去除原始数据源配置，原始数据源配置采用zebra原有数据源配置
 * 4. 支持添加MyBatis的basePackage配置
 */
package com.guosen.zebra.database.sharding.conf;
