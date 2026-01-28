pipeline {
    agent any

    environment {
        IMAGE_NAME = "suryadasari31/mealbox-order-service"
        NEXUS_URL  = "http://172.25.224.1:8082"
        NEXUS_REPO = "mealbox-maven-snapshots"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean test package'
            }
        }

        stage('Publish SNAPSHOT to Nexus') {
            when { branch 'develop' }
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
  </servers>
</settings>
EOF

mvn deploy -DskipTests -s settings.xml
'''
                }
            }
        }

        stage('Docker Build') {
            when { branch 'develop' }
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
docker build \
  -f docker/Dockerfile \
  --build-arg NEXUS_URL=${NEXUS_URL} \
  --build-arg NEXUS_REPO=${NEXUS_REPO} \
  --build-arg NEXUS_USER=${NEXUS_USER} \
  --build-arg NEXUS_PASS=${NEXUS_PASS} \
  -t ${IMAGE_NAME}:${BUILD_NUMBER} .
'''
                }
            }
        }

        stage('Trivy Image Scan') {
            when { branch 'develop' }
            steps {
                sh '''
trivy image \
  --severity HIGH,CRITICAL \
  --exit-code 0 \
  ${IMAGE_NAME}:${BUILD_NUMBER}
'''
            }
        }

        stage('Push Image to Docker Hub') {
            when { branch 'develop' }
            steps {
                withVault([
                    vaultSecrets: [[
                        path: 'secret/mealbox/dockerhub',
                        secretValues: [
                            [envVar: 'DOCKER_USER', vaultKey: 'username'],
                            [envVar: 'DOCKER_PASS', vaultKey: 'password']
                        ]
                    ]]
                ]) {
                    sh '''
set -e
echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
docker push ${IMAGE_NAME}:${BUILD_NUMBER}
echo "Docker push completed"
'''
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "CI pipeline SUCCESS with security scan"
        }
        failure {
            echo "CI pipeline FAILED"
        }
    }
}

