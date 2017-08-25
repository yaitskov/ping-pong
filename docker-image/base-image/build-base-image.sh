#!/bin/bash

docker rm -fv ppbase || true
docker build -t ppbase .
