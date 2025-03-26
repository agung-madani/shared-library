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
                        echo "Test ${env.appName} is set up" // "Test Initial is set up"
                        env.appName = "Storybook"
                        echo "Test ${env.appName} is set up" // "Test Storybook is set up"
                    }
                }
            }
            stage('Test 2') {
                steps {
                    script {
                        echo "Test ${env.appName} is set up" // "Test Storybook is set up"
                        modify_env()
                    }
                }
            }
        }
    }
}
