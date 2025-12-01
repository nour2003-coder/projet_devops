pipeline {
    agent any

    stages {

        stage('Checkout code') {
            steps {
                // 🟢 ADAPTER SI BESOIN : URL + branche
                git branch: 'main', url: 'git@github.com:Nour-Ghribi/eventsProject.git'
            }
        }

        stage('Build & Unit Tests (JUnit)') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo 'Analyse de la qualité du code...'
                sh '''
                    mvn sonar:sonar \
                      -Dsonar.projectKey=eventsProject \
                      -Dsonar.host.url=http://10.0.2.15:9000 \
                      -Dsonar.login=squ_f9a8079370255fa83be0a15efb4ddc8af96cac84
                '''
            }
        }

        stage('Package (JAR Spring Boot)') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Deploy to Nexus (Mise en place de la version)') {
            steps {
                sh '''
                    mvn deploy -DskipTests \
                      -DaltDeploymentRepository=deploymentRepo::default::http://admin:nexus@10.0.2.15:8081/repository/maven-releases/
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    // 🟢 ADAPTER le nom si besoin
                    sh 'docker build -t nourxgh/eventsproject:1.0.0 .'
                }
            }
        }

        stage('Push Docker Image to DockerHub') {
            steps {
                script {
                    // 🟢 ICI on utilise le credentials DockerHub (Username+Password/PAT)
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-conn',   // ID du credentials Jenkins
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh '''
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                            docker push nourxgh/eventsproject:1.0.0
                        '''
                    }
                }
            }
        }

        stage('Deploy with docker-compose (Spring Boot + MySQL)') {
            steps {
                script {
                    sh 'docker compose down || true'
                    sh 'docker compose up -d'
                }
            }
        }
    }
}
