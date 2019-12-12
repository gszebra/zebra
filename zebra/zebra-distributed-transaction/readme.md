提供分布式事务功能

采用阿里巴巴的seata TCC，当前只支持LocalTCC。

引入此模块的微服务，必须在资源配置中添加如下配置

```properties
zebra.distributed.transaction.service.default.group=seataServerIp:port
```

其他seata默认配置都在jar包defaultSeataConf.properties文件里面，若要进行定制，配置中心配置项添加**zebra.distributed.transaction**前缀即可。

比如默认配置项

```properties
transport.type=TCP
```

要对其定制，则在配置中心添加如下配置即可覆盖默认配置

```properties
zebra.distributed.transaction.transport.type=TCP
```
