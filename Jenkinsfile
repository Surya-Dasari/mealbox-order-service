pipeline {
  agent any

  environment {
    VAULT_ADDR = "http://127.0.0.1:8200"
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Vault Login') {
      steps {
        withCredentials([
          string(credentialsId: 'vault-role-id', variable: 'ROLE_ID'),
          string(credentialsId: 'vault-secret-id', variable: 'SECRET_ID')
        ]) {
          sh '''
            VAULT_TOKEN=$(curl -s \
              --request POST \
              --data '{"role_id":"'"$ROLE_ID"'","secret_id":"'"$SECRET_ID"'"}' \
              $VAULT_ADDR/v1/auth/approle/login | jq -r .auth.client_token)

            echo "VAULT_TOKEN=$VAULT_TOKEN" > vault.env
          '''
        }
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
        VAULT_TOKEN=$(curl -s \
          --request POST \
          --data '{"role_id":"'"$ROLE_ID"'","secret_id":"'"$SECRET_ID"'"}' \
          http://127.0.0.1:8200/v1/auth/approle/login | jq -r .auth.client_token)

        NEXUS_USER=$(curl -s -H "X-Vault-Token:$VAULT_TOKEN" \
          http://127.0.0.1:8200/v1/secret/data/mealbox/ci | jq -r .data.data.nexus_username)

        NEXUS_PASS=$(curl -s -H "X-Vault-Token:$VAULT_TOKEN" \
          http://127.0.0.1:8200/v1/secret/data/mealbox/ci | jq -r .data.data.nexus_password)

        cat <<EOF > settings.xml
<settings>
  <servers>
    <server>
      <id>mealbox-maven-snapshots</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
EOF

        mvn deploy -DskipTests --settings settings.xml
      '''
    }
  }
}

    stage('Docker Build') {
      steps {
        sh 'docker build -t mealbox/order-service:snapshot .'
      }
    }
  }

  post {
    always {
      cleanWs()
    }
  }
}

