withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: '81f0e0bd-57fe-41ed-9443-ffff09c3fcc0', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD']]) {
    sh '''
      echo 'echo nexus_username and nexus_password...'
      echo "$NEXUS_USERNAME"
      echo "$NEXUS_PASSWORD"
    '''
}
