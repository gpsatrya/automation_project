pipeline {
    agent any

    parameters {
        choice(name: 'ACTION', choices: ['init', 'destroy'], description: 'Choose action: init to create VM or destroy to delete VM')
    }

    environment {
        GOOGLE_APPLICATION_CREDENTIALS = '../../secrets/panji-sandbox-49d7ea9ef84b.json'
    }

    stages {
        stage('Clone Repository') {
            steps {
                //cloning repo
                sh 'git config --global http.sslVerify false'
                git branch:'main', credentialsId: 'credential-github', url: 'https://github.com/gpsatrya/automation_project.git'
            }
        }

        stage('Terraform Init') {
            when {
                expression { params.ACTION == 'init' }
            }
            steps {
                script {
                    // Initialize Terraform
                    sh 'terraform init'

                    // Apply Terraform configuration to create VM
                    sh 'terraform apply -auto-approve'
                }
            }
        }

        stage('Terraform Destroy') {
            when {
                expression { params.ACTION == 'destroy' }
            }
            steps {
                script {
                    // Initialize Terraform (necessary before destroy)
                    sh 'terraform init'

                    // Destroy Terraform-managed infrastructure
                    sh 'terraform destroy -auto-approve'
                }
            }
        }
    }

    post {
        always {
            // Langkah-langkah yang selalu dilakukan, terlepas dari status pipeline
            echo 'This will always run'
            cleanWs()  // Clean workspace after build
        }
        success {
            // Langkah-langkah yang dilakukan jika pipeline sukses
            echo 'Pipeline succeeded!'
        }
        failure {
            // Langkah-langkah yang dilakukan jika pipeline gagal
            echo 'Pipeline failed!'
        }
    }
}
