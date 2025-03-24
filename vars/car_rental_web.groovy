def call(Map config = [:]) {
    pipeline {
        agent any

        tools {
            maven 'Maven399'
            jdk 'JDK21'
        }
        
        parameters {
            string(name: "git_repo", defaultValue: "${config.git_repo}", trim: true, description: "Git repository URL")
            
            gitParameter(
                name: 'TAG',
                description: 'Select the tag to build',
                type: 'PT_TAG',                 
                defaultValue: '',               
                branch: 'master',
                tagFilter: '*',                 
                sortMode: 'DESCENDING_SMART',   
                selectedValue: 'NONE',          
                useRepository: "${config.git_repo}"
            )
        }
        
        stages {
            stage('Checkout') {
                steps {
                    checkout scmGit(
                        branches: [[name: "refs/tags/${params.TAG}"]], 
                        extensions: [], 
                        userRemoteConfigs: [[
                            credentialsId: '81f0e0bd-57fe-41ed-9443-ffff09c3fcc0', 
                            url: "${config.git_repo}"
                        ]]
                    )
                    
                    echo "Building from tag: ${params.TAG}"
                }
            }
            
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