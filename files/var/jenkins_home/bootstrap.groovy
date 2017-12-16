job('bootstrap') {
    scm {
      git {
        remote {
          url System.getenv('JENKINS_BOOTSTRAP_REPOSITORY');
          credentials System.getenv('JENKINS_BOOTSTRAP_REPOSITORY_USERNAME') ? System.getenv('JENKINS_BOOTSTRAP_REPOSITORY_USERNAME') : 'git'
        }
        branch System.getenv('JENKINS_BOOTSTRAP_REPOSITORY_BRANCH');
      }
    }
    triggers {
        scm 'H/5 * * * *'
    }
    steps {
        dsl {
            external 'config/**/*.groovy'
        }
    }
    publishers {
    }
}
