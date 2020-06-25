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

Then, please edit 'Deployment' named 'kubernetes-synchronizer' in namespace 'kube-system', update env 'URL' and 'TOKEN' as described in [kubernetes-client](https://github.com/kubesys/kubernetes-client).

## Build

```
cp target/kubernetes-synchronizer-[version]-jar-with-dependencies.jar docker/kubernetes-synchronizer.jar
docker build docker/ -t registry.cn-beijing.aliyuncs.com/kubesys/kubernetes-synchronizer:[version]
```

## Usage

access 'http://IP:32080'

- server: kube-database.kube-system:3306
- username: root
- password: onceas

then you can go to database 'kube', and find the synchronous data. 
If you want to synchronous more data, please edit ConfigMap named kubernetes-synchronizer in namesapce 'kube-system'

```
kubectl edt cm kubernetes-synchronizer -n kube-system
```

If you want to customized database dns, username and password, please modify [kubernetes-synchronizer.yaml](https://raw.githubusercontent.com/kubesys/kubernetes-synchronizer/master/kubernetes-synchronizer.yaml)

## Roadmap

- Prototype
  - 0.1: using mysql-connector-java
  - 0.2: using druid
  - 0.3: support keep alive
  - 0.4: provide http-based query
- Development

