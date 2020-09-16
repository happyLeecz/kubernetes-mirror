# kube-synchronizer

Synchronize Kubernetes resources to mariadb/mysql to support query and recovery.

This project is based on :

- [druid](https://github.com/alibaba/druid): Pooled MySQL Connector/J
- [mysql-connector-java](https://github.com/mysql/mysql-connector-j): MySQL Connector/J
- [kubernetes-client](https://github.com/kubesys/kubernetes-client): Java client for Kubernetes-based systems using JSON styles

## Installation

```
https://raw.githubusercontent.com/kubesys/kubernetes-synchronizer/master/kubernetes-mirror.yaml
```



## Quick start

### install database (if you do not have, otherwise skip it)

```
kubectl apply -f https://raw.githubusercontent.com/kubesys/kubernetes-tools/master/core/database.yaml
```

### edit kubernetes-mirror-[arch].yaml

- Then, please edit 'Deployment' named 'kubernetes-mirror' in namespace 'kube-system', update env 'kubeUrl' and 'token' as described in [kubernetes-client](https://github.com/kubesys/kubernetes-client).
- If you use customized database, please add these attributes

```
env:
- name: mysqlUrl
  value: xxx
- name: user
  value: xxx
- name: pwd
  value: xxx
- name: database
  value: xxx
```

### start service

```
kubectl apply -f https://raw.githubusercontent.com/kubesys/kubernetes-mirror/master/kubernetes-mirror-amd64.yaml

or 

kubectl apply -f https://raw.githubusercontent.com/kubesys/kubernetes-mirror/master/kubernetes-mirror-arm64.yam
```

## Build

```
cp target/kubernetes-synchronizer-[version]-jar-with-dependencies.jar docker/kubernetes-mirror.jar
docker build docker/ -t registry.cn-beijing.aliyuncs.com/kubesys/kubernetes-mirror:[version]
```

## Usage

### mysql-based 

access 'http://IP:30307', if you use default mariadb

- server: kube-database.kube-system:3306
- username: root
- password: onceas


then you can go to database 'kube', and find the synchronous data. 


**Now mariadb/mysql can support JSON, so you can on-demand query JSON** 


For example, 

```
use kube;
select * from pods where JSON_EXTRACT(data, '$.metadata.name') like '%database%'
```

In addition, you can work with json as described in [mysql docs](https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html)

**Note that If you want to synchronous more data, please edit ConfigMap named kubernetes-mirror in namesapce 'kube-system'** 
```
kubectl edt cm kubernetes-synchronizer -n kube-system
```

## Roadmap

- 1.5.0 support customized database

