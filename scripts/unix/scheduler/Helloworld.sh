#!/bin/sh

echo
echo --- HelloWorld ----------------------------------------------

  SCHEDULER_URL=$3
  TASK=$1
  SLEEP=$2


workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.extra.scheduler.examples.HelloWorld $TASK $SLEEP $SCHEDULER_URL

echo

