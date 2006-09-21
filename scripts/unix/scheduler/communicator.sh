#!/bin/sh

echo
echo --- Communicator ----------------------------------------------

if [ $# -lt 1 ]; then
    SCHEDULER_URL=//localhost:1234/SchedulerNode
else
  SCHEDULER_URL=$1
fi

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD -Dproactive.rmi.port=1234 org.objectweb.proactive.scheduler.Communicator $SCHEDULER_URL

echo

