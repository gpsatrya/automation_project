pipeline {
    agent any

    parameters {
        choice(name: 'ACTION', choices: ['init', 'destroy'], description: 'Choose action: init to create VM or destroy to delete VM')
    }

    environment {
        GOOGLE_APPLICATION_CREDENTIALS = credentials('gcp-credentials-json')
        TERRAFORM_STATE_PATH = '/var/terraform_state/terraform.tfstate'
    }

    stages {
        stage('Clone Repository') {
            steps {
                //cloning repo
                sh 'git config --global http.sslVerify false'
                git branch:'main', credentialsId: 'credential-github', url: 'https://github.com/gpsatrya/automation_project.git'
            }
        }

        stage('Check State File') {
            steps {
                script {
                    // Check if the terraform.tfstate file exists in the mounted volume
                    sh """
                        if [ -f ${TERRAFORM_STATE_PATH} ]; then
                            echo "State file exists at ${TERRAFORM_STATE_PATH}"
                        else
                            echo "State file does not exist at ${TERRAFORM_STATE_PATH}"
                        fi
                    """
                }
            }
        }

        stage('Terraform Init') {
            steps {
                script {
                    sh 'pwd'
                    dir('terraform') {
                        // Initialize Terraform with the backend state path
                        sh "terraform init -backend-config='path=${TERRAFORM_STATE_PATH}'"
                    }
                }
            }
        }

        // stage('Terraform Plan') {
        //     when {
        //         expression { params.ACTION == 'plan' }
        //     }
        //     steps {
        //         script {
        //             // Navigate to the directory containing main.tf
        //             dir('terraform') {
        //                 // Plan Terraform configuration
        //                 sh "terraform plan -state=${TERRAFORM_STATE_PATH}"
        //             }
        //         }
        //     }
        // }

        stage('Terraform Apply') {
            when {
                expression { params.ACTION == 'init' }
            }
            steps {
                script {
                    // Navigate to the directory containing main.tf
                    dir('terraform') {
                        // Apply Terraform configuration to create VM
                        sh "terraform apply -var 'google_application_credentials=${GOOGLE_APPLICATION_CREDENTIALS}' -auto-approve -state=${TERRAFORM_STATE_PATH}"
                    }
                }
            }
        }

        stage('Terraform Destroy') {
            when {
                expression { params.ACTION == 'destroy' }
            }
            steps {
                script {
                    dir('terraform') {
                        // Initialize Terraform (necessary before destroy)
                        sh 'terraform init'

                        // Destroy Terraform-managed infrastructure
                        sh "terraform destroy -var 'google_application_credentials=${GOOGLE_APPLICATION_CREDENTIALS}' -auto-approve -state=${TERRAFORM_STATE_PATH}"
                    }
                }
            }
        }
    }

    post {
        always {
            // Langkah-langkah yang selalu dilakukan, terlepas dari status pipeline
            echo 'This will always run'
            // cleanWs()  // Clean workspace after build
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
