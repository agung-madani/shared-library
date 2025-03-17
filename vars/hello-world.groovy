@Library("shared-library@main")_

def call() {
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
}