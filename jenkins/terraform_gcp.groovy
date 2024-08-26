pipeline {
    agent any

    environment {
        // GOOGLE_APPLICATION_CREDENTIALS = credentials('gcp-credentials-json')
        TERRAFORM_STATE_PATH = '/var/terraform_state/terraform.tfstate'
    }

    stages {
        stage("Setup Properties") {
            steps {
                script {
                    properties ([
                        parameters ([
                            choice(name: 'ACTION', choices: ['init', 'destroy'], description: 'Choose action: init to create VM or destroy to delete VM'),
                            string(defaultValue: 'panji-sandbox', name: 'ID_PROJECT', trim: true),
                            choice(defaultValue: 'us-central1', name: 'REGION', choices: ['us-central1', 'us-east1'], description: 'Choose Region: Region is permanent choose carefully'),
                            choice(defaultValue: 'us-central1-a', name: 'ZONE', choices: ['us-central1-a', 'us-central1-b', 'us-central1-c', 'us-central1-f', 'us-east1-b', 'us-east1-c', 'us-east1-d'], description: 'Choose Zone: Zone is permanent choose carefully'),
                            string(defaultValue: 'auto-instance', name: 'INSTANCE_NAME', trim: true),
                            choice(defaultValue: 'e2-small', name: 'INSTANCE_TYPE', choices: ['e2-micro', 'e2-small', 'e2-medium'], description: 'Choose Instance Type: Micro=1GB, Small=2GB, Medium=4GB'),
                            choice(defaultValue: 'debian-cloud/debian-11', name: 'INSTANCE_OS', choices: ['debian-cloud/debian-11', 'ubuntu-os-cloud/ubuntu-2004-lts', 'windows-cloud/windows-server-2019-dc'], description: 'Choose Instance OS: Minimum size Linux=10GB, Windows=50GB'),
                            string(defaultValue: '10', name: 'DISK_SIZE', trim: true),
                            string(defaultValue: '5', name: 'INSTANCE_COUNT', trim: true),
                            string(defaultValue: 'default', name: 'VPC_NAME', trim: true),
                            string(defaultValue: 'default', name: 'SUBNET_NAME', trim: true),
                            string(defaultValue: '', name: 'ADDITIONAL_APPLICATION', trim: true)
                        ])
                    ])
                    currentBuild.displayName = "Compute-Engine-${ACTION}-${BUILD_NUMBER}"
                }
            }
        }

        stage('Clone Repository') {
            steps {
                //cloning repo
                sh 'git config --global http.sslVerify false'
                git branch:'main', credentialsId: 'credential-github', url: 'https://github.com/gpsatrya/automation_project.git'
            }
        }

        stage("Terraform Variables Setup") {
            steps {
                script {
                    sh '''
cat <<EOF > terraform-${ACTION}-${BUILD_NUMBER}.tfvars
### For General Value
gcp_project     = "${ID_PROJECT}"
region          = "${REGION}"
zone            = "${ZONE}"

### For Compute Engine Value
instance_name   = "${INSTANCE_NAME}"
instance_os     = "${INSTANCE_OS}"
instance_type   = "${INSTANCE_TYPE}"
disk_size       = "${DISK_SIZE}"
instance_count  = "${INSTANCE_COUNT}"

### For VPC Network Value
vpc_name        = "${VPC_NAME}"

### For Subnet Value
subnet_name     = "${SUBNET_NAME}"
EOF
'''.stripIndent()
                    sh '''
cat terraform-${ACTION}-${BUILD_NUMBER}.tfvars
'''
                    sh '''
ls -la
pwd
'''
                }
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
                        withCredentials([file(credentialsId: 'gcp-credentials-json', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                            // Set the GOOGLE_APPLICATION_CREDENTIALS environment variable and run Terraform apply
                                // export GOOGLE_APPLICATION_CREDENTIALS=$GOOGLE_APPLICATION_CREDENTIALS
                            sh '''
                                export TF_VAR_google_application_credentials=$GOOGLE_APPLICATION_CREDENTIALS
                                terraform apply -auto-approve -state=${TERRAFORM_STATE_PATH}
                            '''
                        }
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
                        // sh "terraform destroy -var 'google_application_credentials=${GOOGLE_APPLICATION_CREDENTIALS}' -auto-approve -state=${TERRAFORM_STATE_PATH}"
                        withCredentials([file(credentialsId: 'gcp-credentials-json', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                            // Set the GOOGLE_APPLICATION_CREDENTIALS environment variable and run Terraform apply
                                // export GOOGLE_APPLICATION_CREDENTIALS=$GOOGLE_APPLICATION_CREDENTIALS
                            sh '''
                                export TF_VAR_google_application_credentials=$GOOGLE_APPLICATION_CREDENTIALS
                                terraform destroy -auto-approve -state=${TERRAFORM_STATE_PATH}
                            '''
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            // Langkah-langkah yang selalu dilakukan, terlepas dari status pipeline
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
