#!/bin/sh

if [ -d ../../SCHEDULER_DB ]
then
rm -rf ../../SCHEDULER_DB
rm ../../.logs/derby.log
fi

workingDir=`dirname $0`

. $workingDir/startScheduler.sh $@
