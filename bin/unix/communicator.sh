#!/bin/sh

echo
echo --- Communicator ----------------------------------------------

workingDir=`pwd`
. ./env.sh

$JAVACMD org.ow2.proactive.scheduler.examples.AdminCommunicator $@


echo

