#!/bin/bash

echo Purge containers by name
for container in ping-pong-mysql ping-pong; do
    echo remove container $container
    docker rm -fv $container || echo "Failed to remove container $container"
done

echo "Purge containers on ports [$@]"

for port in $@ ; do
    docker ps | grep "$port->$port" | while read id rest ; do
        echo "Remove container $id"
        docker rm -fv $id || echo "Failed to remove container $id"
    done
done
