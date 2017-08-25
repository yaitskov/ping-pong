FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y nginx locales nmap emacs inetutils-ping
COPY run.sh /run.sh
COPY jdk1.8.0_131 /jdk
COPY jetty-distribution-9.4.6.v20170531 /jetty

RUN groupadd -r jetty && useradd -r -g jetty jetty
ENV JAVA_HOME /jdk
ENV JETTY_HOME /jetty
ENV PATH $JAVA_HOME/bin:$JETTY_HOME/bin:$PATH
#ENTRYPOINT ["/bin/bash"]
WORKDIR $JETTY_HOME

ENV JETTY_BASE /var/lib/jetty
RUN mkdir -p "$JETTY_BASE"
WORKDIR $JETTY_BASE

# Get the list of modules in the default start.ini and build new base with those modules, then add setuid
RUN modules="$(grep -- ^--module= $JETTY_HOME/start.ini | cut -d= -f2 | paste -d, -s)" \
	&& set   -xe \
	&& java  -jar "$JETTY_HOME/start.jar" --create-startd --add-to-start="$modules,setuid"

ENV JETTY_RUN /run/jetty
ENV JETTY_STATE $JETTY_RUN/jetty.state
ENV TMPDIR /tmp/jetty
RUN set -xe \
	&& mkdir -p "$JETTY_RUN" "$TMPDIR" \
	&& chown -R   jetty:jetty "$JETTY_RUN" "$TMPDIR" "$JETTY_BASE"


RUN localedef -c -i en_US -f UTF-8 en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8
ENV JVM_OPTS "-XX:+DisableExplicitGC -server -verbose:gc -XX:+PrintGCDateStamps -Duser.timezone=GMT -Xms200m -Xmx300m -XX:-UseGCOverheadLimit -XX:-OmitStackTraceInFastThrow -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/oom/heap.dump $JVM_OPTS"
#ENTRYPOINT ["/run.sh"]
RUN mkdir /oom
VOLUME /oom
