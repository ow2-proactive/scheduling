#!/bin/bash
#
# Kills the entire process tree of each process which matches the specified
# pattern. Any process which does not belong the current user will be ingored.

LOGIN=$(whoami)

######## SEND SIGTERM
for ppid in `pgrep -u $LOGIN -f $PROCESS_KILL_TOKEN`;
do
  for pid in `pstree -p $ppid | grep -o -E [0-9]+`
  do
    kill -15 $pid > /dev/null 2>&1
  done
done


###### WAIT FOR PROCESSES TO BE STOPPED
# Iterate in seconds
for iteration in $(seq 1 $1)
do
    if [ `pgrep -u $LOGIN -f $PROCESS_KILL_TOKEN -c` -eq "0" ]
    # means nothing else than the killing procedure is running
    then
        break # Processes don't exist -> so don't wait
    fi
    sleep 1s # Sleep one seconds, because timeout is expressed in seconds
done


##### SEND SIGKILL AFTER TIMEOUT HIT
for ppid in `pgrep -u $LOGIN -f $PROCESS_KILL_TOKEN`;
do
  for pid in `pstree -p $ppid | grep -o -E [0-9]+`
  do
      echo "Send SIGKILL to $pid"
    kill -9 $pid > /dev/null 2>&1
  done
done
