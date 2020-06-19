# kube-synchronizer

Synchronize Kubernetes resources to mariadb to support query and recovery.

This project is based on [mysql-connector-java](https://github.com/mysql/mysql-connector-j).

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
  - 0.2: using mybaits
  - 0.3: using /etc/kubernetes/admin.conf

