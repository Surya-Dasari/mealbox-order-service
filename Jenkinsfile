pipeline {
    agent any

    environment {
        APP_NAME        = "order-service"
        IMAGE_NAME      = "suryadasari31/mealbox-order-service"
        IMAGE_TAG       = "${BRANCH_NAME}-${GIT_COMMIT.take(7)}"

        NEXUS_URL       = "http://172.25.224.1:8082"
        NEXUS_REPO      = "mealbox-maven-snapshots"
        GROUP_ID        = "com/mealbox"
        VERSION         = "0.0.1-SNAPSHOT"

        OC_API          = "https://api.sandbox-m2.ll9k.p1.openshiftapps.com"
        OC_PROJECT      = "default"   // Sandbox uses existing project only
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
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'nexus-creds',
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
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

mvn deploy -s settings.xml
'''
                }
            }
        }

        stage('Docker Build') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'nexus-creds',
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    sh '''
docker build \
  --build-arg NEXUS_URL=${NEXUS_URL} \
  --build-arg NEXUS_REPO=${NEXUS_REPO} \
  --build-arg GROUP_PATH=${GROUP_ID} \
  --build-arg ARTIFACT_ID=${APP_NAME} \
  --build-arg VERSION=${VERSION} \
  --build-arg NEXUS_USER=${NEXUS_USER} \
  --build-arg NEXUS_PASS=${NEXUS_PASS} \
  -t ${IMAGE_NAME}:${IMAGE_TAG} .
'''
                }
            }
        }

        stage('Trivy Image Scan') {
            steps {
                sh '''
trivy image --severity HIGH,CRITICAL --exit-code 0 ${IMAGE_NAME}:${IMAGE_TAG}
'''
            }
        }

        stage('Push Image to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
docker push ${IMAGE_NAME}:${IMAGE_TAG}
'''
                }
            }
        }

        stage('Deploy to OpenShift Sandbox') {
            steps {
                withCredentials([string(
                    credentialsId: 'openshift-token',
                    variable: 'OC_TOKEN'
                )]) {
                    sh '''
/usr/bin/oc login ${OC_API} --token=${OC_TOKEN} --insecure-skip-tls-verify=true
/usr/bin/oc project ${OC_PROJECT}

/usr/bin/oc apply -f k8s/deployment.yaml
/usr/bin/oc apply -f k8s/service.yaml
'''
                }
            }
        }
    }

    post {
        success {
            echo "MealBox Order Service pipeline SUCCESS"
        }
        failure {
            echo "MealBox Order Service pipeline FAILED"
        }
        always {
            cleanWs()
        }
    }
}

