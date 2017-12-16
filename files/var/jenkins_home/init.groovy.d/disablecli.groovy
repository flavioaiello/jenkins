import jenkins.*
import jenkins.model.*
import hudson.model.*

println("Disabling Jenkins CLI...")
jenkins.CLI.get().setEnabled(false)

println("Disabled JNLP protocols...")
def p = AgentProtocol.all()
p.each { x ->
    if (x.name && x.name.contains("JNLP")) {
        println("Removing protocol ${x.name}")
        p.remove(x)
    }
}

println("Disabling JNLP tcp port...")
Jenkins.instance.setSlaveAgentPort(-1)
