#!/bin/bash

mkdir target
cp -r ../ui/dist target/webroot/
cp -r ../ping-pong/target/ping-pong-1.0.0-SNAPSHOT.war  target/ROOT.war
docker build -t pp  .
