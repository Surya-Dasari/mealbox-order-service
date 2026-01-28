pipeline {
    agent any

    environment {
        SERVICE_NAME = "order-service"
        IMAGE_NAME   = "mealbox/order-service"
        MAVEN_OPTS   = "-Dmaven.test.skip=false"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Tests') {
            steps {
                sh '''
                  mvn clean test package
                '''
            }
        }

        stage('Publish SNAPSHOT to Nexus') {
            when {
                branch 'develop'
            }
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
cat > settings.xml <<EOF
<settings>
  <servers>
    <server>
      <id>mealbox-nexus-snapshots</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
    <server>
      <id>mealbox-nexus-releases</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
EOF

mvn deploy -DskipTests -s settings.xml
'''
                }
            }
        }

        stage('Docker Build') {
            when {
                branch 'develop'
            }
            steps {
                sh '''
docker build \
  -f docker/Dockerfile \
  -t ${IMAGE_NAME}:${BUILD_NUMBER} .
'''
            }
        }
    }

    post {
        success {
            echo "Order Service CI pipeline SUCCESS"
        }
        failure {
            echo "Order Service CI pipeline FAILED"
        }
        always {
            cleanWs()
        }
    }
}

