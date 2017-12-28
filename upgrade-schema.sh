#!/bin/bash

PORT=${PORT:-3307}
DBHOST=${DBHOST:-localhost:3306}
SSH=${SSH:-diaitskov@pp}

if nmap localhost -p $PORT | grep $PORT | grep -q open ; then
    echo "port $PORT is busy"
    exit 1
fi

ssh -fN -L $PORT:$DBHOST $SSH

function closeTunel() {
    echo close tunel
    netstat -tlnp 2>&1  |grep 3307  |  gawk  '{print $NF}' | gawk -F'/' '{print $(NF-1)}' | sort  | uniq | while read pid ; do
        echo kill pid $pid;
        kill -9 $pid;
    done
}

trap closeTunel EXIT

for i in `seq 1 10`; do
    if nmap localhost -p $PORT | grep $PORT | grep -q open ; then
        mvn -pl setup-env/docker-containers -P upgrade-db -Dmysql.port=$PORT -Dmysql.host=localhost install
        exit 0
    fi
    echo Wait for port
    sleep 1
done

echo failed to upgrade due timeout
exit 1
