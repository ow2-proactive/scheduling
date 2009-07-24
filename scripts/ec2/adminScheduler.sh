#!/bin/bash

IP_ADDRESS=$(curl http://whatismyip.com/automation/n09230945NL.asp 2>/dev/null)

cd $(dirname $0)/../../bin/unix
./adminScheduler.sh -u http://$IP_ADDRESS:8096/
