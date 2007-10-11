#!/bin/sh

echo
echo --- Communicator ----------------------------------------------

SCHEDULER_URL=$1

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.extra.scheduler.examples.AdminCommunicator $SCHEDULER_URL


echo

