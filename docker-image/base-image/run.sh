#!/bin/bash

export PATH=/jdk/bin/:$PATH
export JAVA_HOME=/jdk
export JETTY_HOME=/jetty

service nginx start

while read line ; do
    if [[ "$line" =~ ^_JP_AS_IS_([^=]+)=(.*)$ ]] ; then
        v="-D$(echo ${BASH_REMATCH[1]})=${BASH_REMATCH[2]}"
        ARGS+=( $v )
    elif [[ "$line" =~ ^_JP([^=]+)=(.*)$ ]] ; then
        v="-D$(echo ${BASH_REMATCH[1]} | tr A-Z_ a-z.)=${BASH_REMATCH[2]}"
        ARGS+=( $v )
    fi
done < <(set -o posix ; set | grep -e '^_JP';)

java -version # log jvm version
set  # log environment variables

function printArgs() {
    while [ $# -ne 0 ] ; do
        echo "*) $1"
        shift
    done
}

echo original args:
printArgs "$@"
echo extended args:
printArgs "${ARGS[@]}"

exec java $JVM_OPTS ${ARGS[@]} -jar /jetty/start.jar
