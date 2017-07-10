#!/bin/bash

cp ../../../../target/ping-pong-1.0.0-SNAPSHOT.war ROOT.war
docker build -t ping-pong:1 .
rm ROOT.war
