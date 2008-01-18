#!/bin/sh

echo
echo --- Communicator ----------------------------------------------

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD -Xms128m -Xmx2048m org.objectweb.proactive.extensions.scheduler.examples.AdminCommunicator $1 $2 $3


echo

