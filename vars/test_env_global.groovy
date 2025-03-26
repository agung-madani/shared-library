def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            appName = "Initial" // Define it in script scope
        }

        stages {
            stage('Test') {
                steps {
                    script {
                        echo "Test ${appName} is set up"
                        appName = "Storybook" // Update the script variable
                        echo "Test ${appName} is set up"
                    }
                }
            }
            stage('Test 2') {
                steps {
                    script {
                        echo "Test ${appName} is set up"
                        modify_env(appName) // Pass the updated value
                    }
                }
            }
        }
    }
}
