@Library("shared-library")_

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