#!/bin/bash

#SCHEDULER_HOME=$(cd ../ && pwd)
SCHEDULER_HOME=/home/Gleb/git/fork/jrochas/scheduling

. config.ini
. util.sh

MIN_WORKERS=$1
MAX_WORKERS=$2

STEP=10
if [ $# -ge 3 ]
then
  STEP=$3
fi

N_WORKERS=$MIN_WORKERS

while [ $N_WORKERS -le $MAX_WORKERS ]
do
#	testTaskRecovery
    let "N_WORKERS=N_WORKERS+$STEP"
done
