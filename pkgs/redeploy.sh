#/bin/bash
# export VERSION="v1"
export ENV="dev"

while (( "$#" )); do
    case $1 in
        -e | --env)
          echo "Environment: $2"
          ENV="$2"
          shift
       ;;
       -h | --help)
            echo "Script usage"
            echo "   -e or --env: Defines the environment. Currently supported: dev, dev2, uat, prod  ___ DEFAULT: dev"
            exit;
        ;;
    esac
shift

echo "Redeploy environment: $ENV"
helm delete rewura-stack -n rewura-$ENV
helm install rewura-stack . -n rewura-$ENV -f values-$ENV.yaml --timeout 10m0s

done



# if [[ "$VERSION" == "v1" ]]; then
#     helm delete rewura-stack -n rewura-$ENV
#     helm install rewura-stack . -n rewura-$ENV -f values-$ENV.yaml --timeout 10m0s
# fi

# if [[ "$VERSION" == "v2" ]]; then
#     helm delete rewura-stack -n rewura-dev2
#     helm install rewura-stack . -n rewura-dev2 -f values-dev-v2.yaml --timeout 10m0s
# fi