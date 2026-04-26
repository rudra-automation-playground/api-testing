pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    environment {
        ENV = "qa"
        BROWSER = "chrome"
    }

    stages {

        stage('Clean Workspace') {
            steps {
                sh 'mvn clean'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Start Docker Grid') {
            steps {
                sh 'docker-compose up -d'
            }
        }

        stage('Run UI Tests') {
            steps {
                sh "mvn test -DsuiteXmlFile=testng.xml -Dbrowser=${BROWSER} -Denv=${ENV}"
            }
        }

        stage('Run API Tests') {
            steps {
                sh "mvn test -Dgroups=api -Denv=${ENV}"
            }
        }

        stage('Run DB Validation') {
            steps {
                sh "mvn test -Dgroups=db -Denv=${ENV}"
            }
        }

        stage('Generate Report') {
            steps {
                sh 'mvn allure:report'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'reports/**'
        }
        success {
            echo "Build Successful ✅"
        }
        failure {
            echo "Build Failed ❌"
        }
        cleanup {
            sh 'docker-compose down'
        }
    }
}