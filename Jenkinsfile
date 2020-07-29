pipeline {
  agent any
  stages {
    stage('Clean') {
      steps {
        sh 'gradle clean'
      }
    }

    stage('BootJar') {
      steps {
        sh 'gradle bootJar'
      }
    }

    stage('') {
      parallel {
        stage('Archive') {
          steps {
            archiveArtifacts(artifacts: 'build/libs/*.jar', excludes: 'build/libs/original-*.jar')
          }
        }

        stage('Generate Docs') {
          steps {
            sh 'gradle dokka'
          }
        }

      }
    }

    stage('Archive Docs') {
      steps {
        archiveArtifacts 'build/javadoc/'
      }
    }

  }
  tools {
    jdk 'openjdk11'
    gradle 'gradle5'
  }
}