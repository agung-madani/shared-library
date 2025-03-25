def call(Map config = [:]) {
    pipeline {
        agent any

        tools {
            nodejs 'Node20'
        }

        environment {
            OCP_PROJECT = "armada009-dev"
            OCP_CLUSTER_URL = "https://api.rm2.thpm.p1.openshiftapps.com:6443"
            OCP_TOKEN = "sha256~y7E_DwDmzSlhc-Ss4CfTZdmcX9T2CX3bJi8wVF1QAKo"
            OCP_REGISTRY = "default-route-openshift-image-registry.apps.rm2.thpm.p1.openshiftapps.com"
            OCP_USER = "armada009"
            QUAY_REGISTRY = "quay.io/armada009"
            QUAY_REPO = "rental-car-web"
            QUAY_USERNAME = "armada009"
            QUAY_PASSWORD = ";4Qi68Rp"
        }

        parameters {
            string(name: "git_repo", defaultValue: "${config.git_repo}", trim: true, description: "Git repository URL")

            gitParameter(
                name: 'TAG',
                description: 'Select the tag to build',
                type: 'PT_TAG',                 
                defaultValue: '',               
                branch: 'main',
                tagFilter: '*',                 
                sortMode: 'DESCENDING_SMART',   
                selectedValue: 'NONE',          
                useRepository: "${config.git_repo}"
            )
        }

        stages {
            stage('Cleanup') {
                steps { cleanWs() }
            }

            stage('Checkout') {
                steps {
                    script {
                        try {
                            checkout([
                                $class: 'GitSCM',
                                branches: [[name: "refs/tags/${params.TAG}"]],
                                userRemoteConfigs: [[
                                    credentialsId: '81f0e0bd-57fe-41ed-9443-ffff09c3fcc0',
                                    url: "git@github.com:agung-madani/bcr-fe-cicd.git"
                                ]]
                            ])
                            echo "Checked out tag: ${params.TAG}"
                        } catch (err) {
                            error "Checkout failed: ${err.getMessage()}"
                        }
                    }
                }
            }

            stage('Application Check') {
                steps {
                    script {
                        try {
                            sh 'node -v'
                            sh 'npm -v'
                            
                            def appName = sh(script: 'node -p "require(\'./package.json\').name"', returnStdout: true).trim()
                            def appFullVersion = sh(script: 'node -p "require(\'./package.json\').version"', returnStdout: true).trim()
                            
                            echo "Application Info: ${appName} - ${appFullVersion}"
                        } catch (err) {
                            error "Application check failed: ${err.getMessage()}"
                        }
                    }
                }
            }

            stage('Install Dependencies') {
                steps {
                    script {
                        sh """
                            npm install --legacy-peer-deps
                        """
                    }
                }
            }

            stage('Build Package') {
                steps {
                    script {
                        sh "npm run build"
                    }
                }
            }

            stage('Build & Push Docker Image') {
                steps {
                    script {
                        def appName = sh(script: 'node -p "require(\'./package.json\').name"', returnStdout: true).trim()
                        def appFullVersion = sh(script: 'node -p "require(\'./package.json\').version"', returnStdout: true).trim()
                            
                        def imageTag = "${env.appName}:${appFullVersion}"
                        def ocpImageTag = "${env.OCP_REGISTRY}/${env.OCP_PROJECT}/${imageTag}"
                        sh """
                            set -x
                            set -e
                            docker build -t ${imageTag} .
                            docker tag ${imageTag} ${ocpImageTag}
                            docker login -p ${env.OCP_TOKEN} -u ${env.OCP_USER} ${env.OCP_REGISTRY}
                            docker push ${ocpImageTag}
                        """
                    }
                }
            }

            stage('Docker Image Archive') {
                steps {
                    script {
                        def appName = sh(script: 'node -p "require(\'./package.json\').name"', returnStdout: true).trim()
                        def appFullVersion = sh(script: 'node -p "require(\'./package.json\').version"', returnStdout: true).trim()
                            
                        def imageTag = "${env.appName}:${appFullVersion}"
                        def ocpImageTag = "${env.OCP_REGISTRY}/${env.OCP_PROJECT}/${imageTag}"
                        def quayImageTag = "${env.QUAY_REGISTRY}/${env.QUAY_REPO}/${imageTag}"
                        sh """
                            set -x
                            set -e
                            docker login -p ${env.OCP_TOKEN} -u ${env.OCP_USER} ${env.OCP_REGISTRY}
                            docker pull ${ocpImageTag}
                            docker tag ${ocpImageTag} ${quayImageTag}
                            docker login -u="${env.QUAY_USERNAME}" -p="${env.QUAY_PASSWORD}" quay.io
                            docker push ${quayImageTag}
                        """
                    }
                }
            }

            stage('OCP ConfigMap') {
                steps {
                    script {
                        def appName = sh(script: 'node -p "require(\'./package.json\').name"', returnStdout: true).trim()
                        def appFullVersion = sh(script: 'node -p "require(\'./package.json\').version"', returnStdout: true).trim()
                        def gitCommitId = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                        sh """
                            set -x
                            set -e
                            export APP_CONFIG_DATA='key-value'
                            oc login --token=${env.OCP_TOKEN} --server=${env.OCP_CLUSTER_URL}
                            oc project ${env.OCP_PROJECT}
                            oc process -f cicd/configmap.yaml -n ${env.OCP_PROJECT} \
                            -p APP_NAME=${appName} -p APP_FULL_VERSION=${appFullVersion}\
                            -p GIT_COMMIT_ID=${gitCommitId} -p JENKINS_BUILD_NUMBER=${env.BUILD_NUMBER}\
                            -p CONFIG_DATA=${env.APP_CONFIG_DATA} | oc apply -n ${env.OCP_PROJECT} -f -
                        """
                    }
                }
            }

            stage('Deploy to OpenShift') {
                steps {
                    script {
                        try {
                            def appName = sh(script: 'node -p "require(\'./package.json\').name"', returnStdout: true).trim()
                            def appFullVersion = sh(script: 'node -p "require(\'./package.json\').version"', returnStdout: true).trim()
                            def gitCommitId = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                            sh """
                                set -x
                                set -e  
                                oc login --token=${env.OCP_TOKEN} --server=${env.OCP_CLUSTER_URL}
                                oc project ${env.OCP_PROJECT}

                                oc process -f cicd/deployment.yaml -n ${env.OCP_PROJECT} \
                                -p APP_NAME=${appName} -p APP_FULL_VERSION=${appFullVersion}\
                                -p GIT_COMMIT_ID=${gitCommitId} -p JENKINS_BUILD_NUMBER=${env.BUILD_NUMBER}\
                                -p CONFIG_DATA=${env.APP_CONFIG_DATA} -p PROJECT_NAME=${env.OCP_PROJECT}\
                                | oc apply -n ${env.OCP_PROJECT} --force=true -f -
                                oc rollout restart ${appName} -n ${env.OCP_PROJECT}

                                echo "Deployment successful"
                            """
                        } catch (err) {
                            error "OpenShift deployment failed: ${err.getMessage()}"
                        }
                    }
                }
            }
        }

        post {
            success { echo "Deployment successful" }
            failure { echo "Deployment failed" }
        }
    }
}
