
FROM ping-pong-jetty:9

COPY scratch.xml ROOT.war /var/lib/jetty/webapps/
RUN mkdir /var/lib/jetty/static
COPY static/* /var/lib/jetty/static
