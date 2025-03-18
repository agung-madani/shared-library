
def call(Map config = [:]) {
    // Set default values if not provided
    def branch = config.branch ?: 'master'
    def credentialsId = config.credentialsId
    def extensions = config.extensions ?: []
    def url = config.url

    // Validate required parameters
    if (!url) {
        error "Git checkout requires 'url' parameter"
    }

    // Create the branch configuration
    def branches = [[name: "*/${branch}"]]
    
    // Create remote config with optional credentials
    def userRemoteConfig = [url: url]
    if (credentialsId) {
        userRemoteConfig.put('credentialsId', credentialsId)
    }
    
    // Perform the checkout
    return checkout(
        scmGit(
            branches: branches,
            extensions: extensions,
            userRemoteConfigs: [userRemoteConfig]
        )
    )
}