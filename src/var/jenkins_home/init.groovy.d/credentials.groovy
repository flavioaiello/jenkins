import jenkins.model.*
import hudson.model.*
import hudson.security.*
import org.jenkinsci.plugins.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*

try {
    globalDomain = Domain.global()
    credentialsStore = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

    /* Credentials to connect to Git repo */
    addGitCredentials(globalDomain)

    /* Credentials to connect to Docker registry */
    addDockerLoginCredentials(globalDomain)
}
catch (Exception e) {
    println("Exception: " + e.message)
}

/**
 * Add git credentials
 *
 * @param globalDomain
 * @return
 */
def addGitCredentials(globalDomain) {
    newGitCredentials = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, "git", "git", new BasicSSHUserPrivateKey.UsersPrivateKeySource(), null, "Private key for accessing git")
    gitUsernameMatcher = CredentialsMatchers.withUsername("git")
    availableSSHCredentials = CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, Jenkins.getInstance(), hudson.security.ACL.SYSTEM, new SchemeRequirement("ssh"))
    existingGitCredentials = CredentialsMatchers.firstOrNull(availableSSHCredentials, gitUsernameMatcher)
    if (existingGitCredentials != null) {
        credentialsStore.updateCredentials(globalDomain, existingGitCredentials, newGitCredentials)
    } else {
        credentialsStore.addCredentials(globalDomain, newGitCredentials)
    }
}

/**
 * Add docker registry credentials
 *
 * @param globalDomain
 * @return
 */
def addDockerLoginCredentials(globalDomain) {
    availableUsernamePwdCredentials = CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, Jenkins.getInstance())
    dockerLoginCredentialsMatcher = CredentialsMatchers.withUsername("docker")
    dockerLoginExistingCredentials = CredentialsMatchers.firstOrNull(availableUsernamePwdCredentials, dockerLoginCredentialsMatcher)
    dockerLoginCredentials = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, "docker", "Docker Login Configuration",  System.getenv('REGISTRY_USERNAME'),  System.getenv('REGISTRY_PASSWORD'))
    if (dockerLoginExistingCredentials != null) {
        credentialsStore.updateCredentials(globalDomain, dockerLoginExistingCredentials, dockerLoginCredentials)
    }
    else {
        credentialsStore.addCredentials(globalDomain, dockerLoginCredentials)
    }
}
