@Library("shared-library")

pipeline {
    agent any
    stages {
        stage('Hello World') {
            steps {
                script {
                    hello()
                }
            }
        }
    }
}