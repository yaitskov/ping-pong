#!/bin/bash

. validate.sh

scp $CLOUD_SPORT_ACCOUNT:~/bin/jetty-distribution-9.3.9.v20160517/logs/* logs
