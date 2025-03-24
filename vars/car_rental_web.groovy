def call(Map config = [:]) {
    pipeline {
        agent any

        tools {
            nodejs 'Node20'
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
                steps {
                    cleanWs()
                }
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
                            sh 'rm -rf node_modules package-lock.json'  // Remove cache
                            sh 'npm install'

                            // Debugging: Print latest package.json
                            sh 'cat package.json'
                            
                            // Get latest values
                            def appName = sh(script: 'node -p "require(\'./package.json\').name"', returnStdout: true).trim()
                            def appFullVersion = sh(script: 'node -p "require(\'./package.json\').version"', returnStdout: true).trim()
                            def appMajorVersion = appFullVersion.tokenize('.')[0]
                            def gitCommitId = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                            
                            echo "Application Info: ${appName}--${appFullVersion}--${appMajorVersion}--${gitCommitId}"
                        } catch (err) {
                            error "Application check failed: ${err.getMessage()}"
                        }
                    }
                }
            }


            // stage('Install Dependencies') {
            //     steps {
            //         script {
            //             try {
            //                 sh 'npm install'
            //             } catch (err) {
            //                 error "Install dependencies failed: ${err.getMessage()}"
            //             }
            //         }
            //     }
            // }
        }
        
        post {
            success {
                echo "Successfully built and processed tag: ${params.TAG}"
            }
            failure {
                echo "Failed to build tag: ${params.TAG}"
            }
        }
    }
}