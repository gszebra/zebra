# 分库分表Demo

## 说明

根据用户ID分库
fund_id % 2

根据时间分表，每个月一张表，对应关系如下
* 201901 -> t_credit_201901
* 201902 -> t_credit_201902
* ...
* 203912 -> t_credit_203912


## 表结构

```SQL
create table t_credit_201909(
    id varchar(30) PRIMARY KEY,
    fund_id bigint,
    m_month int,
    d_day int,
    other_field_1 varchar(30),
    other_field_2 varchar(30)
);

create table t_credit_201908(
    id varchar(30) PRIMARY KEY,
    fund_id bigint,
    m_month int,
    d_day int,
    other_field_1 varchar(30),
    other_field_2 varchar(30)
);
```

## 配置

在配置中心新建配置com.guosen.zebra.demo.sharding.CreditService。
请根据实际情况修改数据源的连接、用户名、密码信息

```properties
zebra.grpc.registryAddress=http://etcdIp1:2379,http://etcdIp2:2379,http://etcdIp3:2379
zebra.grpc.port=50003
opentracing.upd.addr=zipkinIp:9411/api/v2/spans
zebra.database.url[0]=jdbc:sqlserver://sqlServerIp:1433;database=dbName0
zebra.database.username[0]=username
zebra.database.pwd[0]=password
zebra.database.dataSourceName[0]=ds0
zebra.database.url[1]=jdbc:sqlserver://sqlServerIp:1433;database=dbName1
zebra.database.username[1]=username
zebra.database.pwd[1]=password
zebra.database.dataSourceName[1]=ds1
zebra.database.shardingcfg.shardDs01.datasource.names=ds0,ds1
zebra.database.shardingcfg.shardDs01.basePackage=com.guosen.zebra.sample.sharding.dao
zebra.database.shardingcfg.shardDs01.sharding.tables.t_credit.actual-data-nodes=ds$->{0..1}.t_credit_$->{2019..2039}0$->{1..9},ds$->{0..1}.t_credit_$->{2019..2039}$->{10..12}
zebra.database.shardingcfg.shardDs01.sharding.tables.t_credit.table-strategy.inline.sharding-column=m_month
zebra.database.shardingcfg.shardDs01.sharding.tables.t_credit.table-strategy.inline.algorithm-expression=t_credit_$->{m_month}
zebra.database.shardingcfg.shardDs01.sharding.tables.t_credit.database-strategy.inline.sharding-column=fund_id
zebra.database.shardingcfg.shardDs01.sharding.tables.t_credit.database-strategy.inline.algorithm-expression=ds$->{fund_id % 2}
zebra.database.shardingcfg.shardDs01.props.sql.show=true
```
