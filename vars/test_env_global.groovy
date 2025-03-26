@Library('shared-library@main')_

def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            appName = "Initial"   
        }

        stages {
            stage('Test') {
                steps {
                    script {
                        modify_env()
                    }
                }
            }
            stage('Test 2') {
                steps {
                    script {
                        echo "Test ${appName} is set up"
                    }
                }
            }
        }
    }
}