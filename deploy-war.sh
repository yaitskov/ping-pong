#!/bin/bash

. validate.sh

WARDIR=bin/jetty-distribution-9.3.9.v20160517/base/webapps

scp ping-pong/target/ping-pong-1.0.0-SNAPSHOT.war \
        $CLOUD_SPORT_ACCOUNT:~/$WARDIR/../ROOT-next-$$.war || exit 1

ssh $CLOUD_SPORT_ACCOUNT ./deploy-war.sh $$
