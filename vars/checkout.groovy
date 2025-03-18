def call(Map config = [:]) {
    pipeline {
        agent any

        stages {
            stage('Checkout') {
                steps {
                    script {
                        checkout([$class: 'GitSCM', branches: [[name: "${params.git_branch}"]], userRemoteConfigs: [[url: "${params.git_repo}", credentialsId: 'agung-github']]])
                    }
                }
            }
        }
    }
}