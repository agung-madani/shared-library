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