# kube-synchronizer

Synchronize Kubernetes resources to mariadb to support query and recovery.

This project is based on :

- [mysql-connector-java](https://github.com/mysql/mysql-connector-j): MySQL Connector/J
- [druid](https://github.com/alibaba/druid): Pooled MySQL Connector/J
- [kubernetes-httpfrk](https://github.com/kubesys/kubernetes-httpfrk): extends Spring framework to support Kubernetes' lifecycle management

## Installation

```
https://raw.githubusercontent.com/kubesys/kubernetes-synchronizer/master/kubernetes-synchronizer.yaml
```

## Build

```
cp target/kubernetes-synchronizer-[version]-jar-with-dependencies.jar docker/kubernetes-synchronizer.jar
docker build docker/ -t registry.cn-beijing.aliyuncs.com/kubesys/kubernetes-synchronizer:[version]
```

## Roadmap

- Prototype
  - 0.1: using mysql-connector-java
  - 0.2: using druid
  - 0.3: support JSON spec
- Development
  - 1.1: support spring-based query
  - 1.2: support websocket

