
![Zebra](https://blobscdn.gitbook.com/v0/b/gitbook-28427.appspot.com/o/assets%2F-Lv9kABKyc-YZt6JRrpL%2F-LvdaRbCgUftknipL2A1%2F-LvdaVGIpArub0K3v1jn%2Flogo2.png?alt=media)

# 背景

互联网时代，在极端情况下，每天都有新需求要开发上线。随着代码量及团队成员的增加， 传统单体式架构的弊端日益凸显，严重制约了业务的快速创新和敏捷交付，与互联网所追求的 “唯快不破”的目标越来越远。这就是微服务架构兴起的时代大背景。

## 开源目标

分享国信证券在微服务架构和 CNCF 上的实践，让有相同目标方向的尽量少走弯路。

## Zebra 微服务设计原则

* 服务无状态，快速打包，独立部署
* 轻量级通信
* API 网关独立（即将开源），不对底层服务进行依赖
* 基础设施自动化
* 高度可观察
* 去中心化
* 隐藏内部实现细节
* 调用失败隔离

# Zebra 微服务架构

## 架构图如下

![架构图](https://blobscdn.gitbook.com/v0/b/gitbook-28427.appspot.com/o/assets%2F-Lv9kABKyc-YZt6JRrpL%2F-LvFAE0H9QHWQGRe4Re_%2F-LvFIp6h45giEe0T5C-a%2Fzebra%E6%9E%B6%E6%9E%84.png?alt=media)

## 组件说明

组件|说明
:--|:--|
配置中心|微服务配置统一管理、版本支持、配置分离，保证服务无状态
注册中心|服务自动注册、自动发现、负载均衡、异常保护、异常通报下发、服务降级
服务中心|展示服务状态，服务依赖关系，服务 API 管理，监控展示。
监控中心|基于 Prometheus 协议，实现应用监控、异常上报、功能主动监控
Zebra微服务|基于 JDK 1.8 和 SpringBoot 的基础上进行研发，极大简化开发。

## Zebra 微服务开发框架

基于 JDK 1.8和 SpringBoot 的基础上进行研发，极大简化开发。具有如下功能：

1. 采用grpc拦截器形式进行权限管理，服务接口授权；
2. 采用令牌池方案，进行服务端并发控制；
3. 采用阿里巴巴 TtlExecutors 线程池技术，管理业务线程，保障线程数据上下文不窜包；
4. 使用线程池技术，当服务并发量超过服务最大线程数，服务过载快速失败；
5. 采用 opentracing 标准，进行调用链分析埋点；
6. 采用 JDK1.8 CompletableFuture 技术超时管理、 实现高性能异步调用和实现高性能延迟返回；
7. 采用 SpringBoot 动态配置技术，简化数据库、缓存、消息队列访问；
8. 基于 gRPC 流模式技术，实现流模式支持，实现服务推送；
9. 基于 TTL 技术实现跨进程上下文传递；
10. 自研延时加载技术，实现 Slow Start 特性，避免服务启动流量直接打到服务上；
11. 基于netty优雅下线技术以及 JDK 推出钩子技术实现架构优雅下线，避免停服务时有请求未处理完。

# 文档
请参考 https://www.kancloud.cn/gszebra/zebra_doc/content

# QQ 群

欢迎加入 Zebra QQ 群共同探讨~

![QQ 群](https://blobscdn.gitbook.com/v0/b/gitbook-28427.appspot.com/o/assets%2F-Lv9kABKyc-YZt6JRrpL%2F-LvdaXBX-gtg9vaFUlZ-%2F-LvdcwnIjKT--4Fox29A%2Fzebra_qq_group.jpg?alt=media)
