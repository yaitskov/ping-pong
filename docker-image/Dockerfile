FROM ppbase:latest

COPY target/webroot /webroot
COPY target/ROOT.war /var/lib/jetty/webapps/
COPY nginx.conf /etc/nginx/sites-available/default

CMD bash /run.sh
