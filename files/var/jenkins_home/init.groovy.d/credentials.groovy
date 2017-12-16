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

    /* Generic Username Password Credentials */
    addUsernamePasswordCredentials(globalDomain)
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
 * Add generic credentials
 *
 * @param globalDomain
 * @return
 */
def addUsernamePasswordCredentials(globalDomain) {

    def credentials = System.getenv('JENKINS_BASIC_CREDENTIALS')

    // Backward compatibility for docker registry specific credentials
    credentials += System.getenv('REGISTRY_USERNAME') ? " docker:" + System.getenv('REGISTRY_USERNAME') + ":" + System.getenv('REGISTRY_PASSWORD') : ""

    for(credential in credentials.split()) {

        availableUsernamePwdCredentials = CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, Jenkins.getInstance())
        myLoginCredentialsMatcher = CredentialsMatchers.withUsername(credential.split(':')[0])
        myLoginExistingCredentials = CredentialsMatchers.firstOrNull(availableUsernamePwdCredentials, myLoginCredentialsMatcher)
        myLoginCredentials = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, credential.split(':')[0], "Generic credentials", credential.split(':')[1], credential.split(':')[2])

        if (myLoginExistingCredentials != null) {
            println("update credentials with userid " + credential.split(':')[0] + " and username " + credential.split(':')[1])
            credentialsStore.updateCredentials(globalDomain, myLoginExistingCredentials, myLoginCredentials)
        }
        else {
            println("create credentials with userid " + credential.split(':')[0] + " and username " + credential.split(':')[1])
            credentialsStore.addCredentials(globalDomain, myLoginCredentials)
        }
    }
}
