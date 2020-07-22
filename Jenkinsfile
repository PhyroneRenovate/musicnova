pipeline {
    agent any

    stages {
        stage("Clean") {
            steps {
                sh "gradle clean"
            }
        }
        stage("Prepare") {
            steps {
                sh "gradle first-install"
            }
        }
        stage("BootJar") {
            steps {
                sh "gradle bootJar"
            }
        }
        stage("Archive") {
            steps {
                archiveArtifacts(artifacts: 'build/libs/*.jar', excludes: 'build/libs/original-*.jar')
            }
        }

    }
    tools{
        jdk 'openjdk11'
        gradle 'gradle5'
    }
}