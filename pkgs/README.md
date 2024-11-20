# Deployment to DEV

```
  helm install rewura-stack . \
       -n rewura-dev \
       -f values-dev.yaml \
       --timeout 10m0s

helm delete rewura-stack
V1: 
helm install rewura-stack . -n rewura-dev -f values-dev.yaml --timeout 10m0s

V2: 
helm install rewura-stack . -n rewura-dev2 -f values-dev-v2.yaml --timeout 10m0s
```

```
kubectl config get-contexts
kubectl config use-context do-fra1
kubectl config set-context --current --namespace=rewura-dev2
```

# Infrastructure

### Mongo
```
helm install mongo-mongodb bitnami/mongodb --set auth.username=root --set auth.password=HgtDPZDW2v --set auth.database=appointment
```

## Auth
http://localhost:8302/oauth2/authorize/google