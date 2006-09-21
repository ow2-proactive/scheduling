#!/bin/sh

echo
echo --- Scheduler ----------------------------------------------

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh


$JAVACMD -Dproactive.rmi.port=1234 org.objectweb.proactive.scheduler.Scheduler

echo
echo ------------------------------------------------------------
