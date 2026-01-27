pipeline {
  agent any

  stages {
    stage('Vault Smoke Test') {
      steps {
        withVault([
          vaultSecrets: [[
            path: 'secret/mealbox/ci',
            secretValues: [
              [envVar: 'NEXUS_USER', vaultKey: 'nexus_username'],
              [envVar: 'NEXUS_PASS', vaultKey: 'nexus_password']
            ]
          ]]
        ]) {
          sh '''
            echo "Vault injection working"
            test -n "$NEXUS_USER"
            test -n "$NEXUS_PASS"
          '''
        }
      }
    }
  }
}

