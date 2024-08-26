pipeline {
    agent any

    parameters {
        choice(name: 'ACTION', choices: ['init', 'destroy'], description: 'Choose action: init to create VM or destroy to delete VM')
    }

    environment {
        GOOGLE_APPLICATION_CREDENTIALS = credentials('gcp-credentials-json')
        TERRAFORM_STATE_PATH = '/var/terraform_state/terraform.tfstate'
        GPG_KEY_ID = 'gsatrya'
    }

    stages {
        stage('Clone Repository') {
            steps {
                //cloning repo
                sh 'git config --global http.sslVerify false'
                git branch:'main', credentialsId: 'credential-github', url: 'https://github.com/gpsatrya/automation_project.git'
            }
        }

        stage('Setup') {
            steps {
                script {
                    sh 'pwd'
                    sh 'ls ../../../../terraform_state'
                    sh 'ls /var/'
                    sh 'ls /home/'
                    // Decrypt the state file before running Terraform
                    sh "gpg --output ${TERRAFORM_STATE_PATH} --decrypt ${TERRAFORM_STATE_PATH}.gpg"
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

        stage('Cleanup') {
            steps {
                script {
                    // Encrypt the state file after running Terraform
                    sh "gpg --output ${TERRAFORM_STATE_PATH}.gpg --encrypt --recipient ${GPG_KEY_ID} ${TERRAFORM_STATE_PATH}"
                    // Remove the unencrypted state file
                    sh "rm ${TERRAFORM_STATE_PATH}"
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
