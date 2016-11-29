FROM alpine:latest

ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk/jre
ENV JENKINS_HOME /var/jenkins_home
ENV JENKINS_SLAVE_AGENT_PORT 50000
ENV JENKINS_VERSION 2.7.4
ENV DOCKER_COMPOSE_VERSION 1.8.0

# Add scripts and plugin list
ADD src /

# Packages
RUN apk add --no-cache --repository http://dl-cdn.alpinelinux.org/alpine/edge/main && \
    apk add --no-cache --repository  http://dl-cdn.alpinelinux.org/alpine/edge/community && \
    apk update && \
    apk upgrade && \
    apk add ca-certificates supervisor openjdk8 bash git curl zip wget docker ttf-dejavu jq coreutils openssh && \
    rm -rf /var/cache/apk/*

# Docker compose
RUN echo "Installing docker-compose ..." && \
    curl -sSL --create-dirs --retry 1 https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose && \
    chmod +x /usr/local/bin/docker-compose

# Jenkins
RUN echo "Installing jenkins ${JENKINS_VERSION} ..." && \
    curl -sSL --create-dirs --retry 1 http://repo.jenkins-ci.org/public/org/jenkins-ci/main/jenkins-war/${JENKINS_VERSION}/jenkins-war-${JENKINS_VERSION}.war -o /usr/share/jenkins/jenkins.war

# Jenkins solve plugin dependencies from plugins.txt
RUN echo "Recursive solve and reduce plugin dependencies ..." && \
    bash -c 'curl -sSO https://updates.jenkins-ci.org/current/update-center.actual.json && \
    function solve { \
        for plugin in $(cat update-center.actual.json | jq --arg p "${1%:*}" -r '"'"'.plugins[] | select(.name == $p) | .dependencies[] | select(.optional == false) | .name + ":" + .version'"'"');do \
            echo $plugin >> /var/jenkins_home/plugins.txt; \
            solve $plugin; \
        done \
    } && \
    solve $(tr '"'"'\n'"'"' '"'"' '"'"' < /var/jenkins_home/plugins.txt) && \
    sort -Vr /var/jenkins_home/plugins.txt | sort -u -t: -k1,1 -o /var/jenkins_home/plugins.txt'

# Jenkins install plugins from plugins.txt
RUN while read plugin; do \
    echo "Downloading ${plugin} ..." && \
    curl -sSL --create-dirs --retry 3 https://updates.jenkins-ci.org/download/plugins/${plugin%:*}/${plugin#*:}/${plugin%:*}.hpi -o /var/jenkins_home/plugins/${plugin%:*}.jpi && \
    touch /var/jenkins_home/plugins/${plugin%:*}.jpi.pinned; \
    done < /var/jenkins_home/plugins.txt

EXPOSE 8080 8443 50000

ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
