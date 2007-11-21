#!/bin/sh

echo
echo --- DemoGetResult ----------------------------------------------

  SCHEDULER_URL=$1


workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.extra.scheduler.examples.GetJobResult $SCHEDULER_URL

echo

