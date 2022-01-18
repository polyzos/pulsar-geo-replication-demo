```shell
docker run -p 61616:61616 -p 8161:8161 rmohr/activemq
```

```shell
docker run -it -p 6650:6650 \
  -p 8080:8080 \
  --name pulsar apachepulsar/pulsar:2.9.1 \
  bin/pulsar standalone
```
1. List eu-central cluster
```shell
aws eks list-clusters --region eu-central-1
```

3. Set kubeconfig
```shell
aws eks --region eu-central-1 update-kubeconfig \
  --name sn-reptile-dev-eu-central-1
```
4. Create a namespace
```shell
kubectl create ns pulsar
```

5. Setup 3 pulsar clusters
```shell
helm install \
    --values k8s/values.yaml \
    --set initialize=true \
    pulsar-west apache/pulsar
    
helm install \
    --values k8s/values.yaml \
    --set initialize=true \
    pulsar-east apache/pulsar
    
helm install \
    --values k8s/values.yaml \
    --set initialize=true \
    pulsar-central apache/pulsar
```

6. Get 3 terminals for Pulsar admin
```shell
kubectl exec -it pulsar-west-toolset-0 -n pulsar -- bash
kubectl exec -it pulsar-east-toolset-0 -n pulsar -- bash
kubectl exec -it pulsar-central-toolset-0 -n pulsar -- bash
```

7. List clusters
```shell
bin/pulsar-admin clusters list
```

8. Get the clusters
```shell
bin/pulsar-admin clusters get pulsar-west
bin/pulsar-admin clusters get pulsar-east
bin/pulsar-admin clusters get pulsar-central
```

9. Register east and central cluster to west
```shell
bin/pulsar-admin clusters create \
  --broker-url http://pulsar-east-broker.pulsar.svc.cluster.local:8080/ \
  --url pulsar://pulsar-east-broker.pulsar.svc.cluster.local:6650/ \
  pulsar-east
  
bin/pulsar-admin clusters create \
  --broker-url http://pulsar-central-broker.pulsar.svc.cluster.local:8080/ \
  --url pulsar://pulsar-central-broker.pulsar.svc.cluster.local:6650/ \
  pulsar-central
```

9. Register west and central cluster to east
```shell
bin/pulsar-admin clusters create \
  --broker-url http://pulsar-west-broker.pulsar.svc.cluster.local:8080/ \
  --url pulsar://pulsar-west-broker.pulsar.svc.cluster.local:6650/ \
  pulsar-west
  
bin/pulsar-admin clusters create \
  --broker-url http://pulsar-central-broker.pulsar.svc.cluster.local:8080/ \
  --url pulsar://pulsar-central-broker.pulsar.svc.cluster.local:6650/ \
  pulsar-central
```

10. Register west and east cluster to central
```shell
bin/pulsar-admin clusters create \
  --broker-url http://pulsar-west-broker.pulsar.svc.cluster.local:8080/ \
  --url pulsar://pulsar-west-broker.pulsar.svc.cluster.local:6650/ \
  pulsar-west
  
bin/pulsar-admin clusters create \
  --broker-url http://pulsar-east-broker.pulsar.svc.cluster.local:8080/ \
  --url pulsar://pulsar-east-broker.pulsar.svc.cluster.local:6650/ \
  pulsar-east
```

11. Create tenants and namespaces on all 3 clusters
```shell
bin/pulsar-admin tenants create testt --allowed-clusters pulsar-east,pulsar-west,pulsar-central
bin/pulsar-admin namespaces create testt/testns --clusters pulsar-east,pulsar-west,pulsar-central
```

12. Run consumers on east and central clusters and the producer on the west cluster
```shell
bin/pulsar-client consume -s "sub" testt/testns/t1 -n 0

bin/pulsar-client produce  testt/testns/t1  --messages "hello from the west cluster" -n 10
```

```shell
kubectl port-forward service/pulsar-central-proxy 8080:8080 6650:6650 -n pulsar
kubectl port-forward service/pulsar-east-proxy 8081:8080 6651:6650 -n pulsar
kubectl port-forward service/pulsar-west-proxy 6652:6650 -n pulsar
```

```shell
bin/pulsar zookeeper-shell
```

Streamnative Platform
======================
1. Set kubeconfig for central
```shell
aws eks list-clusters --region eu-central-1

aws eks --region eu-central-1 update-kubeconfig \
  --name sn-tarpon-dev-eu-central-1
```

2. Set kubeconfig for west
```shell
aws eks list-clusters --region eu-west-1

aws eks --region eu-west-1 update-kubeconfig \
  --name sn-bee-dev-eu-west-1
```

4. Install SN Platform
```shell
helm upgrade --install vault-operator banzaicloud-stable/vault-operator -n pulsar
helm upgrade --install cert-manager jetstack/cert-manager --set installCRDs=true -n pulsar
helm upgrade --install pulsar-operator streamnative/pulsar-operator -n pulsar
helm upgrade --install function-mesh streamnative/function-mesh-operator -n pulsar 

helm install -f k8s/sn/values.yaml pulsar-central streamnative/sn-platform --set initialize=true -n pulsar
helm install -f k8s/sn/values.yaml pulsar-west streamnative/sn-platform --set initialize=true -n pulsar
```

5. Get the secret
```shell
kubectl get secret pulsar-central-sn-platform-vault-console-admin-passwd \
  -o=jsonpath='{.data.password}' \
   -n pulsar | base64 --decode; echo
   
kubectl get secret pulsar-west-sn-platform-vault-console-admin-passwd \
  -o=jsonpath='{.data.password}' \
   -n pulsar | base64 --decode; echo
   
Root token: 9IjZ7UBeovB6
```

6. Get west cluster info
```shell
# Get the root token
kubectl get secret pulsar-west-sn-platform-vault-secret-env-injection \
  -o=jsonpath='{.data.brokerClientAuthenticationParameters}' \
  -n pulsar | base64 --decode; echo

Yzc2NzQzMjItNDI5Mi01OWFlLWJiYWEtMjU0M2U4YWZhZDQ1OmVjMWZiODNmLTA0NWEtOTYxNi1hOTMwLTdkZjI5ZjEzOTQyNAo=

# Go into the toolset pod 
kubectl exec -it pulsar-west-sn-platform-toolset-0 -n pulsar -- bash

# List the clusters
pulsar-admin clusters list

pulsar-west-sn-platform

# Get the cluster info
pulsar-admin clusters get pulsar-west-sn-platform
```

6. Get central cluster info
```shell
# Get the root token
kubectl get secret pulsar-central-sn-platform-vault-secret-env-injection \
  -o=jsonpath='{.data.brokerClientAuthenticationParameters}' \
  -n pulsar | base64 --decode; echo

NDIwYzhhNTAtMzc4OS0yODE5LWQyZjItZGM5Yjc3NzQzYmJiOjc0NmRmN2M3LWJhYzAtYTM3YS1hMzgxLWMwODE1ZDc0YWQxNAo=

# Go into the toolset pod 
kubectl exec -it pulsar-central-sn-platform-toolset-0 -n pulsar -- bash

# List the clusters
pulsar-admin clusters list

pulsar-central-sn-platform

# Get the cluster info
pulsar-admin clusters get pulsar-central-sn-platform
```

7. Register the west to central
```shell
bin/pulsar-admin clusters create \
  --broker-url pulsar://<west-url>:6650 \
  --url http://<west-url>:8080 \
  --auth-plugin org.apache.pulsar.client.impl.auth.AuthenticationToken \
  --auth-parameters token:<west-token>=\
  pulsar-west-sn-platform
```

8. Register the central to west
```shell
bin/pulsar-admin clusters create \
  --broker-url pulsar://<central-url>:6650 \
  --url http://<central-url>:8080 \
  --auth-plugin org.apache.pulsar.client.impl.auth.AuthenticationToken \
  --auth-parameters token:<central-token>\
  pulsar-central-sn-platform
```

9. Create resources
```shell
bin/pulsar-admin tenants create testt --allowed-clusters pulsar-west-sn-platform,pulsar-central-sn-platform
bin/pulsar-admin namespaces create testt/testns --clusters pulsar-west-sn-platform,pulsar-central-sn-platform
```