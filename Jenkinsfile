pipeline {
  agent any

  environment {
    VAULT_ADDR = "http://127.0.0.1:8200"
    NEXUS_REPO_ID = "mealbox-maven-snapshots"
    NEXUS_REPO_URL = "http://nexus.local:8082/repository/mealbox-maven-snapshots"
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Test') {
      parallel {

        stage('Compile') {
          steps {
            sh 'mvn clean compile -DskipTests'
          }
        }

        stage('Unit Tests') {
          steps {
            sh 'mvn test'
          }
        }
      }
    }

    stage('Publish SNAPSHOT') {
      steps {
        withCredentials([
          string(credentialsId: 'vault-role-id', variable: 'ROLE_ID'),
          string(credentialsId: 'vault-secret-id', variable: 'SECRET_ID')
        ]) {
          sh '''
            set -e

            echo "🔐 Logging into Vault"
            VAULT_TOKEN=$(curl -s \
              --request POST \
              --data '{"role_id":"'"$ROLE_ID"'","secret_id":"'"$SECRET_ID"'"}' \
              $VAULT_ADDR/v1/auth/approle/login | jq -r .auth.client_token)

            echo "📦 Fetching Nexus credentials from Vault"
            NEXUS_USER=$(curl -s -H "X-Vault-Token:$VAULT_TOKEN" \
              $VAULT_ADDR/v1/secret/data/mealbox/ci | jq -r .data.data.nexus_username)

            NEXUS_PASS=$(curl -s -H "X-Vault-Token:$VAULT_TOKEN" \
              $VAULT_ADDR/v1/secret/data/mealbox/ci | jq -r .data.data.nexus_password)

            echo "📝 Generating temporary Maven settings.xml"
            cat <<EOF > settings.xml
<settings>
  <servers>
    <server>
      <id>${NEXUS_REPO_ID}</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
EOF

            echo "🚀 Deploying SNAPSHOT to Nexus"
            mvn deploy -DskipTests --settings settings.xml
          '''
        }
      }
    }

    stage('Docker Build') {
      steps {
        sh '''
          echo "🐳 Building Docker image"
          docker build -t mealbox/order-service:snapshot .
        '''
      }
    }
  }

  post {
    always {
      cleanWs()
    }
    success {
      echo "✅ CI pipeline completed successfully"
    }
    failure {
      echo "❌ CI pipeline failed – check stage logs"
    }
  }
}

