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
                        echo "Test ${appName} is set up"
                    }
                }
            }
            stage('Test 2') {
                steps {
                    script {
                        echo "Test ${appName} is set up"
                        modify_env()
                    }
                }
            }
        }
    }
}