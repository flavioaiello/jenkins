#!groovy

def dockerImageName = env.JOB_NAME
def dockerRegistry = 'https://myprivateregistry'
def dockerCredentialsId = 'myCredentials'

node {
    stage('Checkout') {
        checkout scm
    }

    def versionTag = sh(returnStdout: true, script: 'git describe --all').trim().replaceAll(/(.*\/)?(.+)/, '$2')
    def baseTag = (versionTag == ~/v(\d+.\d+.\d+)/) ? 'latest' : versionTag
    def latestTag = (versionTag == ~/v(\d+.\d+.\d+)/) ? 'latest' : null
    def dockerImage

    stage('Build') {

        docker.withRegistry(dockerRegistry, dockerCredentialsId) {

            echo "*** Set the docker base image tag to inherit from ***"
            sh "sed -i 's/\${BASE_TAG}/${baseTag}/g' Dockerfile"

            echo "*** Set also the docker latest tag if any ***"
            latestTagSuffix = (latestTag) ? '-t ' + dockerImageName + ':' + latestTag : ''

            echo "*** Start building ... ***"
            dockerImage = docker.build dockerImageName + ":${versionTag}", "${latestTagSuffix} ."

            echo "*** Docker image successfully built. ***"
        }
    }

    stage('Push') {

        docker.withRegistry(dockerRegistry, dockerCredentialsId) {

            echo "*** Push version ... ***"
            dockerImage.push(versionTag)

            if (latestTag) {
                echo "*** Push latest ... ***"
                dockerImage.push(latestTag)
            }

            echo "*** Docker image successfully pushed to registry. ***"
        }
    }
}
