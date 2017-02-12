FROM alpine:latest

ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk/jre
ENV JENKINS_HOME /var/jenkins_home
ENV JENKINS_SLAVE_AGENT_PORT 50000
ENV JENKINS_VERSION 2.32.2

# Add scripts and plugin list
COPY files /

# Packages
RUN set -ex && \
    apk add --no-cache --repository http://dl-cdn.alpinelinux.org/alpine/edge/main && \
    apk add --no-cache --repository  http://dl-cdn.alpinelinux.org/alpine/edge/community && \
    apk update && \
    apk upgrade && \
    apk add --no-cache ca-certificates supervisor openjdk8 bash git curl zip wget docker ttf-dejavu jq coreutils openssh py2-pip && \
    echo "*** fix key permissions ***" && \
    chmod 600 /root/.ssh/id_rsa && \
    echo "*** Installing docker-compose ***" && \
    pip install --upgrade pip && \
    pip install docker-compose

# Install Jenkins and plugins from plugins.txt
RUN set -ex && \
    echo "*** Installing jenkins ***" && \
    curl -sSL --create-dirs --retry 1 http://repo.jenkins-ci.org/public/org/jenkins-ci/main/jenkins-war/${JENKINS_VERSION}/jenkins-war-${JENKINS_VERSION}.war -o /usr/share/jenkins/jenkins.war && \
    echo "*** Recursive solve and reduce plugin dependencies ***" && \
    bash -c 'curl -sSO https://updates.jenkins-ci.org/current/update-center.actual.json && \
    function solve { \
        for dependency in $(cat update-center.actual.json | jq --arg p "${1%:*}" -r '"'"'.plugins[] | select(.name == $p) | .dependencies[] | select(.optional == false) | .name + ":" + .version'"'"');do \
            echo $dependency >> /var/jenkins_home/plugins.txt; \
            solve $dependency; \
        done \
    } && \
    for plugin in $(tr '"'"'\n'"'"' '"'"' '"'"' < /var/jenkins_home/plugins.txt);do solve $plugin; done && \
    sort -Vr /var/jenkins_home/plugins.txt | sort -u -t: -k1,1 -o /var/jenkins_home/plugins.txt' && \
    echo "*** Jenkins install plugins from plugins.txt *** " && \
    while read plugin; do \
    echo "*** Downloading ${plugin} ***" && \
    curl -sSL --create-dirs --retry 3 https://updates.jenkins-ci.org/download/plugins/${plugin%:*}/${plugin#*:}/${plugin%:*}.hpi -o /var/jenkins_home/plugins/${plugin%:*}.jpi && \
    touch /var/jenkins_home/plugins/${plugin%:*}.jpi.pinned; \
    done < /var/jenkins_home/plugins.txt

EXPOSE 8080 8443 50000

ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
