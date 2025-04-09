pipeline {
    agent any

    tools{
        jdk 'jdk17'
        maven 'maven3'
    }
    environment {
        SCANNER_HOME= tool 'sonar-scanner'
        APP_NAME = "number-guessing-game"
        DOCKER_USERNAME = "willywan"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Git Checkout') {
            steps {
                git credentialsId: 'git-cred', url: 'https://github.com/William-eng/JavaGame.git'
            }
        }
        stage('Compile') {
            steps {
                sh "mvn compile"
            }
        }
        stage('Test') {
            steps {
                sh "mvn test"
            }
        }
        stage('File System Scan') {
            steps {
                sh "trivy fs --format table -o trivy-fs-report.html ."
            }
        }
        stage('Code Quality Analysis') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh ''' $SCANNER_HOME/bin/sonar-scanner -Dsonar-projectName=${APP_NAME} -Dsonar.projectKey=${APP_NAME} \
                            -Dsonar.java.binaries=. '''
                }
            }
        }
        stage('Quality Gate') {
            steps {
                script {
                    waitForQualityGate abortPipeline: false, credentialsId: 'sonar-token'
                }
            }
        }
        stage('Build') {
            steps {
                sh "mvn package"
            }
        }
        stage('Publish Artifacts to Nexus') {
            steps {
                withMaven(globalMavenSettingsConfig: 'global-settings', jdk: 'jdk17', maven: 'maven3', mavenSettingsConfig: '', traceability: true) {
                    sh "mvn deploy"
                }
            }
        }
        stage('Build & Tag Docker Image') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                        sh "docker build -t ${DOCKER_USERNAME}/${APP_NAME}:${IMAGE_TAG} ."
                        sh "docker tag ${DOCKER_USERNAME}/${APP_NAME}:${IMAGE_TAG} ${DOCKER_USERNAME}/${APP_NAME}:latest"
                    }
                }
            }
        }
        stage('Docker Image Scan') {
            steps {
                sh "trivy image --format table -o trivy-image-report.html ${DOCKER_USERNAME}/${APP_NAME}:${IMAGE_TAG}"
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                        sh "docker push ${DOCKER_USERNAME}/${APP_NAME}:${IMAGE_TAG}"
                        sh "docker push ${DOCKER_USERNAME}/${APP_NAME}:latest"
                    }
                }
            }
        }
        stage('Update Kubernetes Manifests') {
            steps {
                script {
                    sh """
                    sed -i 's|image: ${DOCKER_USERNAME}/${APP_NAME}:.*|image: ${DOCKER_USERNAME}/${APP_NAME}:${IMAGE_TAG}|' k8s-manifest/deployment.yaml
                    """
                }
            }
        }
        stage('Deploy to EKS') {
            steps {
                withAWS(credentials: 'aws-credentials', region: 'us-east-1') {
                    sh "aws eks update-kubeconfig --name your-eks-cluster-name --region us-east-1"
                    sh "kubectl apply -f k8s-manifest/configmap.yaml -n webapps"
                    sh "kubectl apply -f k8s-manifest/deployment.yaml -n webapps"
                    sh "kubectl apply -f k8s-manifest/service.yaml -n webapps"
                }
            }
        }
        stage('Verify the deployment') {
            steps {
                withAWS(credentials: 'aws-credentials', region: 'us-east-1') {
                    sh "kubectl get pods -n webapps | grep ${APP_NAME}"
                    sh "kubectl get svc -n webapps | grep ${APP_NAME}"
                    sh "echo 'Application deployed successfully to EKS cluster'"
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'trivy-fs-report.html, trivy-image-report.html', followSymlinks: false
            junit testResults: '**/target/surefire-reports/*.xml'
            cleanWs()
        }
        success {
            echo 'The Pipeline succeeded!'
        }
        failure {
            echo 'The Pipeline failed!'
        }
    }
}
