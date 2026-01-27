pipeline {
    agent any

    stages {
        stage('Publish SNAPSHOT') {
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
                      echo "Vault vars injected"
                      echo "User: $NEXUS_USER"
                    '''
                }
            }
        }
    }
}

