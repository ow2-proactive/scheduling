#!/bin/bash
#
# Kills the entire process tree of each process which matches the specified
# pattern. Any process which does not belong the current user will be ingored.

LOGIN=$(whoami)
for ppid in `pgrep -u $LOGIN -f $1`;
do
  if [ $ppid -ne $$ ]
  then
    for pid in `pstree -p $ppid | grep -o -E [0-9]+`
    do
      kill -9 $pid > /dev/null 2>&1
    done
  fi
done
