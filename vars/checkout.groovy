def call(Map config = [:]) {
    pipeline {
        agent any

        parameters {
            string(name: "git_repo", defaultValue: config.get("git_repo", ""), trim: true, description: "Git repository URL")
            gitParameter(
                name: "git_branch",
                defaultValue: "",
                type: "PT_TAG",
                branch: "master",
                branchFilter: ".*",
                tagFilter: "*",
                sortMode: "DESCENDING_SMART",
                selectedValue: "TOP",
                quickFilterEnabled: true,
                useRepository: config.get("git_repo", ""),
                listSize: 5
            )
        }

        stages {
            stage('Checkout') {
                steps {
                    script {
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: params.git_branch]],
                            userRemoteConfigs: [[url: params.git_repo, credentialsId: 'agung-github']]
                        ])
                    }
                }
            }
        }
    }
}
