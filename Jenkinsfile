pipeline {
  agent any
  stages {
    stage('Clean') {
      steps {
        sh 'gradle clean'
      }
    }

    stage('BootJar') {
      parallel {
        stage('BootJar') {
          steps {
            sh 'gradle bootJar'
          }
        }

        stage('Create Docs') {
          agent any
          steps {
            sh 'gradle dokka'
          }
        }

      }
    }

    stage('Archive') {
      steps {
        archiveArtifacts(artifacts: 'build/libs/*.jar', excludes: 'build/libs/original-*.jar')
        archiveArtifacts 'build/javadoc/'
      }
    }

  }
  tools {
    jdk 'openjdk11'
    gradle 'gradle5'
  }
}