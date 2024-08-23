pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                //cloning repo
                sh 'git config --global http.sslVerify false'
                git branch:'main', credentialsId: 'credential-github', url: 'https://github.com/gpsatrya/automation_project.git'
            }
        }

        stage('Build') {
            steps {
                // Menjalankan perintah build (misalnya, compile kode)
                sh 'echo "Building the project..."'
                // Tambahkan perintah build lainnya di sini, misalnya: mvn clean install
            }
        }

        stage('Test') {
            steps {
                // Menjalankan unit tests
                sh 'echo "Running tests..."'
                // Tambahkan perintah testing di sini, misalnya: mvn test
            }
        }

        stage('Deploy') {
            steps {
                // Menjalankan perintah deploy
                sh 'echo "Deploying the application..."'
                // Tambahkan perintah deploy lainnya di sini
                // Misalnya, deploy ke server:
                // sh 'scp target/app.jar user@server:/path/to/deploy'
            }
        }
    }

    post {
        always {
            // Langkah-langkah yang selalu dilakukan, terlepas dari status pipeline
            echo 'This will always run'
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
