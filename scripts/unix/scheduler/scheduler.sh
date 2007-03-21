#!/bin/sh

echo
echo --- Scheduler----------------------------------------------


  SCHEDULER_URL=$1
	RM=$2

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.examples.scheduler.LocalSchedulerExample $SCHEDULER_URL $RM

echo

