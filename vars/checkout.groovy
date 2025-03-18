// vars/checkout.groovy - Shared Library Function
def call(Map config = [:]) {
    // Ensure parameters are provided correctly
    def gitRepo = config.get('git_repo', '')

    // Run checkout step
    script {
        checkout([
            $class: 'GitSCM',
            branches: [[name: '*/main']],  // Use "main" as default if branch is not specified
            userRemoteConfigs: [[url: gitRepo, credentialsId: 'agung-github']]
        ])
    }
}
