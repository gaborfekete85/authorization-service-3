#/bin/bash
./rebuild.sh && cd ../../pkgs && ./redeploy.sh -e $1 && cd ../apps/authorization-service
sleep 3
klog auth
