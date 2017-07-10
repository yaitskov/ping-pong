#!/bin/bash

docker rm -fv ping-pong
docker run -it -p 8080:80 -p 5008:5005 --name ping-pong --net ping-pong-integration ping-pong:1
