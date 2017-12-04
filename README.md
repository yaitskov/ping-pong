#  Cloud-Sport application

Cloud-Sport application is designed for conducting sport tournaments
in sports like ping-pong, tennis, squash and badminton.

## Building

Build requires Db schema to be deployed.
MySql is provided by profile setup-env.
Db schema is deployed and upgraded via profile upgrade-db.
So the command for initial build is:
```
mvn clean install -P setup-env -P upgrade-db
```

## Tests

```
mvn clean install
```

```
mvn install -am -pl ping-pong -Dtest=Abc -DfailIfNoTests=false
```

## Simulations

There is a tournament simulator available.  It could be used to setup
a specific situation in a tournament to debug and test UI and server
logic manually.

```
mvn install -DskipTests -P simulate -Dtest=OneCategorySim#tournamentOf2
```

Next logical step is to authenticate as an admin or a participant with dev handle:
```
http://localhost/ddd/#!/emails
```

## Development mode

Launching server side (http port is 8081):
```
export MAVEN_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=5001,server=y,suspend=n"
mvn -pl ping-pong -P run-server -DskipTests
```

Rebuilding ui part:
```
cd ui
grunt
```

Example of Nginx configuration for serving static connected and
forwarding AJAX to the server.
```
server {
	listen 80 default_server;
	listen [::]:80 default_server ipv6only=on;

	index index.html index.htm;

        location / {
           autoindex on;
           root /home/egnyte/pro/ping-pong/code/ui/dist;
        }
        location /api/ {
           proxy_set_header Host \$http_host;
           proxy_pass http://localhost:8081/;
        }

        # just for dev
        location /js/libs {
           autoindex on;
           alias /home/egnyte/pro/ping-pong/code/ui/node_modules;
        }
        location /css/libs {
           autoindex on;
           alias /home/egnyte/pro/ping-pong/code/ui/node_modules;
        }
        # access to authentication without emails
        location /ddd {
           autoindex on;
           alias /home/egnyte/pro/ping-pong/code/ui/src/dev;
        }
}
```
