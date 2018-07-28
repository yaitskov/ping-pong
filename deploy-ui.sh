#!/bin/bash

. validate.sh

scp -r ui/dist  $CLOUD_SPORT_ACCOUNT:~/ping-pong-ui-next-$$ || exit 1

ssh $CLOUD_SPORT_ACCOUNT ./deploy-ui.sh $$
