#/bin/bash
./rebuild.sh && cd ../../pkgs && ./redeploy.sh -e $1 && cd ../apps/authorization-service
sleep 10
kubectl logs `kubectl get pods -n rewura-$1 | grep auth | awk '{print $1}'` -f