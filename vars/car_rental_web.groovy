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
            QUAY_REGISTRY = "quay.io/armada009"
            QUAY_REPO = "rental-car-web"
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
                        def imageTag = "${env.QUAY_REGISTRY}/${env.QUAY_REPO}:${params.TAG}"
                        sh """
                            docker build -t ${imageTag} .
                            docker login -u="armada009" -p=";4Qi68Rp" quay.io
                            docker push ${imageTag}
                        """
                    }
                }
            }

            stage('Deploy to OpenShift') {
                steps {
                    script {
                        try {
                            sh """
                                oc login --token=${env.OCP_TOKEN} --server=${env.OCP_CLUSTER_URL}
                                oc project ${env.OCP_PROJECT}
                                
                                # Ensure deployment exists
                                oc apply -f cicd/deployment.yaml

                                # Now update the image
                                oc set image deployment/react-vite-app react-vite-app=${env.QUAY_REGISTRY}/${env.QUAY_REPO}:${params.TAG}

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
