#!/bin/bash
#
# Kills the entire process tree of each process which matches the specified
# pattern. Any process which does not belong the current user will be ingored.

LOGIN=$(whoami)

parentPid=`ps -o ppid= -p $$`

######## SEND SIGTERM
for ppid in `pgrep -u $LOGIN -f $1`;
do
  if [ $ppid -ne $$ -a $ppid -ne $parentPid ]
  then
    for pid in `pstree -p $ppid | grep -o -E [0-9]+`
    do
      kill -15 $pid > /dev/null 2>&1
    done
  fi
done



###### WAIT FOR PROCESSES TO BE STOPPED
# Iterate in seconds
for iteration in $(seq 1 $2)
do
    if [ `pgrep -u $LOGIN -f $1 -c` -eq "2" ] # command_step.sh executes kill_process.tree.sh, so two processes
    # means nothing else than the killing procedure is running
    then
        break # Processes don't exist -> so don't wait
    fi
    echo "Sleep a second: $iteration"
    sleep 1s # Sleep one seconds, because timeout is expressed in seconds
done




##### SEND SIGKILL AFTER TIMEOUT HIT
for ppid in `pgrep -u $LOGIN -f $1`;
do
  if [ $ppid -ne $$ -a $ppid -ne $parentPid ]
  then
    for pid in `pstree -p $ppid | grep -o -E [0-9]+`
    do
      kill -9 $pid > /dev/null 2>&1
    done
  fi
done