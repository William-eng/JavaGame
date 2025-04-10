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
                 git branch: 'main', credentialsId: 'git-cred', url: 'https://github.com/William-eng/javagame.git'
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
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh """
                         $SCANNER_HOME/bin/sonar-scanner -Dsonar-projectName=java-project -Dsonar.projectKey=java-project -X \
                            -Dsonar.java.binaries=.
                     """
                    }
            }
        }
        // stage('Quality Gate') {
        //     steps {
        //          script {
        //             waitForQualityGate abortPipeline: false, credentialsId: 'sonar-token'
        //         }
        //     }
        // }
        stage('Build') {
            steps {
                sh "mvn package"
            }
        }
        stage('Publish Artifacts to Nexus') {
            steps {
                withMaven(globalMavenSettingsConfig: 'global-settings', jdk: 'jdk17', maven: 'maven3', mavenSettingsConfig: '', traceability: true) {
                    sh "mvn deploy -Dnexus.username=admin -Dnexus.password=resolve"
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
        // stage('Update Kubernetes Manifests') {
        //     steps {
        //         script {
        //             sh """
        //             sed -i 's|image: ${DOCKER_USERNAME}/${APP_NAME}:.*|image: ${DOCKER_USERNAME}/${APP_NAME}:${IMAGE_TAG}|' k8s-manifest/deployment.yaml
        //             """
        //         }
        //     }
        // }
        stage('Deploy to EKS') {
            steps {
                withCredentials([string(credentialsId: 'aws-credentials', variable: 'AWS_ACCESS_KEY_ID'),
                                string(credentialsId: 'aws-secret', variable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh "aws eks update-kubeconfig --name willy-cluster --region us-east-1"
                    sh "kubectl apply -f k8s-manifest/configmap.yaml -n webapps"
                    sh "kubectl apply -f k8s-manifest/deployment.yaml -n webapps"
                    sh "kubectl apply -f k8s-manifest/service.yaml -n webapps"
                }
            }
        }
        stage('Process Security Scan Results') {
            steps {
                script {
                    try {
                        // Extract vulnerability counts from Trivy reports
                        def fsScanCriticalCount = sh(script: "grep -c 'CRITICAL' trivy-fs-report.html || true", returnStdout: true).trim()
                        def fsScanHighCount = sh(script: "grep -c 'HIGH' trivy-fs-report.html || true", returnStdout: true).trim()
                        
                        def imageScanCriticalCount = sh(script: "grep -c 'CRITICAL' trivy-image-report.html || true", returnStdout: true).trim()
                        def imageScanHighCount = sh(script: "grep -c 'HIGH' trivy-image-report.html || true", returnStdout: true).trim()
                        
                        // Store for use in email notifications
                        env.FS_CRITICAL_VULNS = fsScanCriticalCount
                        env.FS_HIGH_VULNS = fsScanHighCount
                        env.IMAGE_CRITICAL_VULNS = imageScanCriticalCount
                        env.IMAGE_HIGH_VULNS = imageScanHighCount
                        
                        echo "Security Scan Summary:"
                        echo "Filesystem: ${fsScanCriticalCount} critical, ${fsScanHighCount} high vulnerabilities"
                        echo "Docker image: ${imageScanCriticalCount} critical, ${imageScanHighCount} high vulnerabilities"
                        
                        // Optionally fail build based on thresholds
                        // if (imageScanCriticalCount.toInteger() > 0) {
                        //     error "Build failed due to ${imageScanCriticalCount} critical vulnerabilities in Docker image!"
                        // }
                    } catch (Exception e) {
                        echo "Failed to process security scan results: ${e.message}"
                    }
                }
            }
        }
    }
    
    post {
        always {
            archiveArtifacts artifacts: 'trivy-fs-report.html, trivy-image-report.html', followSymlinks: false
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
        }
        success {
            echo 'The Pipeline succeeded!'
            mail to: 'williamtijesuni@gmail.com',
                 subject: "[SUCCESS] Pipeline: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: """
                 Pipeline ${env.JOB_NAME} #${env.BUILD_NUMBER} completed successfully!
                 
                 Application: ${APP_NAME}
                 Environment: ${env.ENVIRONMENT ?: 'Production'}
                 Build URL: ${env.BUILD_URL}
                 
                 Security Scan Results:
                 - Filesystem scan: ${env.FS_CRITICAL_VULNS ?: '0'} critical, ${env.FS_HIGH_VULNS ?: '0'} high vulnerabilities
                 - Docker image scan: ${env.IMAGE_CRITICAL_VULNS ?: '0'} critical, ${env.IMAGE_HIGH_VULNS ?: '0'} high vulnerabilities
                 
                 The security scan reports have been archived as build artifacts.
                 Please check the Trivy reports for detailed vulnerability information.
                 
                 Deployment has been completed to EKS cluster.
                 """
        }
        failure {
            echo 'The Pipeline failed!'
            mail to: 'williamtijesuni@gmail.com',
                 subject: "[FAILED] Pipeline: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: """
                 Pipeline ${env.JOB_NAME} #${env.BUILD_NUMBER} failed!
                 
                 Application: ${APP_NAME}
                 Environment: ${env.ENVIRONMENT ?: 'Production'}
                 Build URL: ${env.BUILD_URL}
                 
                 Security Scan Results (if available):
                 - Filesystem scan: ${env.FS_CRITICAL_VULNS ?: 'N/A'} critical, ${env.FS_HIGH_VULNS ?: 'N/A'} high vulnerabilities
                 - Docker image scan: ${env.IMAGE_CRITICAL_VULNS ?: 'N/A'} critical, ${env.IMAGE_HIGH_VULNS ?: 'N/A'} high vulnerabilities
                 
                 Please check the console output for detailed error information.
                 If security scan reports were generated before the failure,
                 they have been archived as build artifacts.
                 """
        }
        cleanup {
            cleanWs()
        }
    }
}
