#!/bin/bash

NETWORK_NAME=$1

docker network ls | grep -q $NETWORK_NAME && exit
docker network create $NETWORK_NAME || {
    echo Failed to create network $NETWORK_NAME; exit 1; }
