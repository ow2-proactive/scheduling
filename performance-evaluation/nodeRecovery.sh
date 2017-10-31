#!/bin/bash

SCHEDULER_HOME=$(cd ../ && pwd)

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

echo "#New series of node recovery tests are started" >> $STORAGE_FILE

while [ $N_WORKERS -le $MAX_WORKERS ]
do
    testNodeRecovery
    let "N_WORKERS=N_WORKERS+$STEP"
done
