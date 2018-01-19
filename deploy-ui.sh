#!/bin/bash

scp -r ui/dist  diaitskov@pp:~/ping-pong-ui-next-$$ || exit 1

ssh diaitskov@pp ./deploy-ui.sh $$
