## 说明

本样例说明如何编写分布式事务代码

## 配置

在配置中心添加名为“com.guosen.zebra.distributed.transaction.cfg”的配置。
添加的配置内容如下：

```properties
zebra.distributed.transaction.service.default.grouplist=127.0.0.1:8091
```

## Seata Server

### 下载
到[seata release页面](https://github.com/seata/seata/releases)下载seata-server。

当前最新版本的链接为[seata-server-0.9.0.zip](https://github.com/seata/seata/releases/download/v0.9.0/seata-server-0.9.0.zip)

### 启动

解压seata-server，根据运行的操作系统执行对应的启动命令，比如Windows为bin目录下的seata-server.bat。
