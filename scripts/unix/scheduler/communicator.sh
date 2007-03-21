#!/bin/sh

echo
echo --- Communicator ----------------------------------------------

if [ $# -lt 1 ]; then
    SCHEDULER_URL=//localhost/SchedulerNode
else
  SCHEDULER_URL=$1
fi

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.extra.scheduler.AdminCommunicator $SCHEDULER_URL


echo

