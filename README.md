# kube-synchronizer

Synchronize Kubernetes resources to mariadb to support query and recovery.

This project is based on :

- [druid](https://github.com/alibaba/druid): Pooled MySQL Connector/J
- [mysql-connector-java](https://github.com/mysql/mysql-connector-j): MySQL Connector/J
- [kubernetes-httpfrk](https://github.com/kubesys/kubernetes-httpfrk): extends Spring framework to support Kubernetes' lifecycle management
- [kubernetes-client](https://github.com/kubesys/kubernetes-client): Java client for Kubernetes-based systems using JSON styles
## Installation

```
https://raw.githubusercontent.com/kubesys/kubernetes-synchronizer/master/kubernetes-synchronizer.yaml
```

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
  <groupId>com.github.kubesys</groupId>
  <artifactId>kubernetes-mirror</artifactId>
  <version>1.6.0</version> 
</dependency>

<repositories>
   <repository>
       <id>pdos-repos</id>
       <name>PDOS Releases</name>
       <url>http://39.106.40.190:8081/repository/maven-public/</url>
    </repository>
</repositories>
```

Then, please edit 'Deployment' named 'kubernetes-synchronizer' in namespace 'kube-system', update env 'URL' and 'TOKEN' as described in [kubernetes-client](https://github.com/kubesys/kubernetes-client).

## Build

```
cp target/kubernetes-synchronizer-[version]-jar-with-dependencies.jar docker/kubernetes-synchronizer.jar
docker build docker/ -t registry.cn-beijing.aliyuncs.com/kubesys/kubernetes-synchronizer:[version]
```

## Usage

### mysql-based 

access 'http://IP:30307'

- server: kube-database.kube-system:3306
- username: root
- password: onceas

then you can go to database 'kube', and find the synchronous data. 
If you want to synchronous more data, please edit ConfigMap named kubernetes-synchronizer in namesapce 'kube-system'

In addition, you can work with json as described in [mysql docs](https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html)

```
kubectl edt cm kubernetes-synchronizer -n kube-system
```

If you want to customized database dns, username and password, please modify [kubernetes-synchronizer.yaml](https://raw.githubusercontent.com/kubesys/kubernetes-synchronizer/master/kubernetes-synchronizer.yaml)

### http-based 

## Roadmap

- Prototype
  - 0.1: using mysql-connector-java
  - 0.2: using druid
  - 0.3: support keep alive
  - 0.6: support rabbitmq
- Development

