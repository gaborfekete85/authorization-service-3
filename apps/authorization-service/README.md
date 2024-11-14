# Build

```
./gradlew clean build publish jib -PDOCKER_REPOSITORY=gabendockerzone
```

# Kube
```
kubectl config get-contexts
kubectl config use-context do-fra1
kubectl config set-context --current --namespace=rewura-dev
```