pipeline {
    agent any

    environment {
        IMAGE_NAME = "suryadasari31/mealbox-order-service"
        IMAGE_TAG  = "${BUILD_NUMBER}"

        NEXUS_URL  = "http://172.25.224.1:8082"
        NEXUS_REPO = "mealbox-maven-snapshots"

        OC_API     = "https://api.sandbox-m2.ll9k.p1.openshiftapps.com"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Test') {
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

        stage('Docker Build (from Nexus)') {
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
  -t ${IMAGE_NAME}:${IMAGE_TAG} .
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
  ${IMAGE_NAME}:${IMAGE_TAG}
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
docker push ${IMAGE_NAME}:${IMAGE_TAG}
'''
                }
            }
        }

        stage('Deploy to OpenShift Sandbox') {
            when { branch 'develop' }
            steps {
                withCredentials([
                    string(credentialsId: 'openshift-token', variable: 'OC_TOKEN')
                ]) {
sh '''
/usr/bin/oc login https://api.sandbox-m2.ll9k.p1.openshiftapps.com \
  --token=$OC_TOKEN \
  --insecure-skip-tls-verify=true
'''

# Sandbox allows only existing project
oc project $(oc projects -q | head -1)

# Inject image dynamically and deploy
sed "s|IMAGE_PLACEHOLDER|${IMAGE_NAME}:${IMAGE_TAG}|g" \
  platform/openshift/order-service/deployment.yaml | oc apply -f -

oc apply -f platform/openshift/order-service/service.yaml
oc apply -f platform/openshift/order-service/route.yaml
'''
                }
            }
        }
    }

    post {
        success {
            echo "MealBox Order Service: CI + Deploy SUCCESS"
        }
        failure {
            echo "MealBox Order Service: CI or Deploy FAILED"
        }
        always {
            cleanWs()
        }
    }
}

