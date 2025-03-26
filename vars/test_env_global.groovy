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
                        echo "Test ${appName} is set up"
                        appName = "Storybook"
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