pipeline {
    agent any

    environment {
        IMAGE_NAME = "suryadasari31/mealbox-order-service"
        IMAGE_TAG  = "${BUILD_NUMBER}"

        NEXUS_URL  = "http://172.25.224.1:8082"
        NEXUS_REPO = "mealbox-maven-snapshots"

        OC_API     = "https://api.rm2.thpm.p1.openshiftapps.com:6443"
        OC_PROJECT = "suryadasari31-dev"
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
set -e

oc login ${OC_API} \
  --token=${OC_TOKEN} \
  --insecure-skip-tls-verify=true

oc project ${OC_PROJECT}

sed "s|IMAGE_PLACEHOLDER|${IMAGE_NAME}:${IMAGE_TAG}|g" \
  platform/openshift/deployment.yaml | oc apply -f -

oc apply -f platform/openshift/service.yaml
oc apply -f platform/openshift/route.yaml
'''
                }
            }
        }

        stage('Helm Template (Dry Run)') {
            when { branch 'develop' }
            steps {
                sh '''
set -e

echo "Cloning MealBox platform repo (Helm charts)..."
rm -rf mealbox-platform || true
git clone https://github.com/Surya-Dasari/mealbox-platform.git

echo "Running Helm dry-run for order-service..."
/usr/local/bin/helm template order-service \
  mealbox-platform/helm/mealbox-backend-service \
  -f mealbox-platform/helm/mealbox-backend-service/values/values-order-service.yaml
'''
            }
        }
    }

    post {
        success {
            echo "MealBox Order Service: Pipeline SUCCESS"
        }
        failure {
            echo "MealBox Order Service: Pipeline FAILED"
        }
        always {
            cleanWs()
        }
    }
}
