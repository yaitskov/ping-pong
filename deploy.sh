#!/bin/bash

WARDIR=bin/jetty-distribution-9.3.9.v20160517/base/webapps

scp -r ui/dist  diaitskov@pp:~/ping-pong-ui-next-$$ && \
    scp ping-pong/target/ping-pong-1.0.0-SNAPSHOT.war \
        diaitskov@pp:~/$WARDIR/../ROOT-next-$$.war || exit 1

ssh diaitskov@pp ./deploy.sh $$
