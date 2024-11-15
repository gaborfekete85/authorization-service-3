def nextEnv(currentEnv) {
    def environments = ["Development", "Production" ] as String[]
    def index = environments.findIndexOf { it == currentEnv }
    if (index == environments.size()-1) {
        return "FINAL_ENV"
    }
    return environments[index+1]
}

def getEnvironment(currentEnv) {
    return currentEnv
}

pipeline {
    agent any
    tools {
        jdk "JDK 11"
    }
    environment {
        NEW_VERSION = '1.3.0'
        KUBE_EXISTS = fileExists '/var/jenkins_home/kubectl'
        HELM_EXISTS = fileExists '/var/jenkins_home/helm'
    }
    parameters {
        choice(name: 'ENVIRONMENT', 
            choices: ["dev", 
                      "staging",
                      "prod"
            ], 'description': 'Target environment. This envs manifest ( values.yaml ) will be updated')
        choice(name: 'SERVICE', 
            choices: ["frontend", "frontend-v2",
                      "hobby-service", "authorization-service", "file-upload-service", "appointment-service"
            ], 'description': 'Target environment. This envs manifest ( values.yaml ) will be updated')
        string(name: 'TAG', defaultValue: 'latest', description: 'The new docker image tag. ')
        string(name: 'REPOSITORY', defaultValue: 'gabendockerzone', description: 'The docker repository where the images are loaded. ')
        string(name: 'DOCKER_IMG_PREFIX', defaultValue: 'rewura-', description: 'To avoid confusion of docker image names. IMPORTANT: Include "-" at the end like "rewura-" ')
    }
    stages {
            stage('Update GIT') {
            steps {
            script {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    withCredentials([usernamePassword(credentialsId: 'ca0bdb6b-4470-45f4-baef-259619495ab8', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                        echo "Env: ${ENVIRONMENT}"
                        echo "Service: ${SERVICE}"
                        echo "TAG: ${TAG}"
                        echo "REPOSITORY: ${REPOSITORY}"
                        sh """
                            git config user.email contact@feketegabor.com
                            git config user.name gaborfekete85
                            cat values-${ENVIRONMENT}.yaml
                            sed -i 's+${REPOSITORY}/${DOCKER_IMG_PREFIX}${SERVICE}.*+${REPOSITORY}/${DOCKER_IMG_PREFIX}${SERVICE}:${TAG}+g' values-${ENVIRONMENT}.yaml
                            cat values-${ENVIRONMENT}.yaml
                            git add .
                            git commit -m 'Done by Jenkins Job changemanifest: ${SERVICE}:${TAG} on ${ENVIRONMENT}'
                            git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${GIT_USERNAME}/rewura-manifest.git HEAD:master
                        """

                        //def encodedPassword = URLEncoder.encode("$GIT_PASSWORD",'UTF-8')
                        //sh "cd authorization-service"
			            //sh "git config user.email contact@feketegabor.com"
                        //sh "git config user.name gaborfekete85"
                        ////sh "git switch master"
                        //sh "cat deployment.yaml"
                        //sh "sed -i 's+gabendockerzone/test-devops.*+gabendockerzone/test-devops:${DOCKERTAG}+g' deployment.yaml"
                        //sh "cat deployment.yaml"
                        //sh "git add ."
                        //sh "git commit -m 'Done by Jenkins Job changemanifest: ${env.BUILD_NUMBER}'"
                        //sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${GIT_USERNAME}/devops-manifest.git HEAD:master"
      }
    }
  }
  }
}
    }
    post {
        always {
            // Always will be executed no matter if FAILED or SEUCCEDED
            echo 'Cleanup'
        }
        success {
            // OnSuccess
            echo 'Success'
        }
        failure {
            // OnFail
            echo 'Failed'
        }
    }
}
