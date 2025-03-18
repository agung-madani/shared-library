def call(Map config = [:]) {
    pipeline {
        agent any
        
        parameters {
            string(name: "git_repo", defaultValue: "${config.git_repo}", trim: true, description: "Git repository URL")
            
             gitParameter(
                name: 'BRANCH_TAG',
                description: 'Select the branch or tag to build',
                type: 'PT_BRANCH_TAG',
                defaultValue: 'master',
                branch: '',
                branchFilter: 'origin/(.*)',
                tagFilter: '*',
                sortMode: 'DESCENDING_SMART',
                selectedValue: 'DEFAULT',        
                useRepository: "${config.git_repo}"
            )

            // Additional parameter example - useful for build configuration
            choice(
                name: 'BUILD_TYPE',
                choices: ['development', 'staging', 'production'],
                description: 'Select the build environment'
            )
        }
        
        stages {
            stage('Checkout') {
                steps {
                    // Using the selected tag parameter for checkout
                    checkout scmGit(
                        branches: [[name: "refs/tags/${params.TAG}"]], // Note the use of refs/tags/ prefix
                        extensions: [], 
                        userRemoteConfigs: [[
                            credentialsId: '81f0e0bd-57fe-41ed-9443-ffff09c3fcc0', 
                            url: "${config.git_repo}"
                        ]]
                    )
                    
                    // Print selected parameters for verification
                    echo "Building from tag: ${params.TAG}"
                    echo "Selected build type: ${params.BUILD_TYPE}"
                }
            }
            
            stage('Build') {
                steps {
                    echo "Building tagged version ${params.TAG} for ${params.BUILD_TYPE} environment..."
                    // Example build command using the parameters
                    sh "echo 'Building tag ${params.TAG} for ${params.BUILD_TYPE}'"
                    // sh 'mvn clean package -P${params.BUILD_TYPE}'
                }
            }
            
            stage('Test') {
                steps {
                    echo "Running tests for tagged version ${params.TAG}..."
                    // Add your test commands here
                }
            }
            
            stage('Deploy') {
                when {
                    expression { params.BUILD_TYPE == 'production' }
                }
                steps {
                    echo "Deploying tagged version ${params.TAG} to ${params.BUILD_TYPE}..."
                    // Tagged versions might typically be deployed to production
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