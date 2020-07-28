pipeline {
    agent {
        docker {
            image 'registry-lab.phyrone.de:443/phyrone/gradle-with-node-docker:jdk11'
        }
    }
    stages {
        stage("Clean") {
            steps {
                sh "gradle clean"
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
}
