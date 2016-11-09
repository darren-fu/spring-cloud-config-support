# spring-cloud-config-support
> Backup config from spring cloud config server.

- 配合Spring Cloud Config，提供配置文件备份功能。
- 分布式系统中，服务启动时，如果启用了分布式配置中心，则将从配置中心获取的配置进行备份。
- 配置中心失效时，使用备份的配置文件启动服务。

### 使用
- 引入源码(不能遗漏/META-INF/spring.factories)或者引入jar包
```
<dependency>
    <groupId>df.open</groupId>
    <artifactId>spring-cloud-config-support</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
- bootstrap.yml加入配置spring.cloud.config.backup，启用备份, eg.
```
spring:
  application:
    name: server
  cloud:
    config:
      uri: http://localhost:7000
      backup:
        enable: true
        file: d:/server-backup.properties
```