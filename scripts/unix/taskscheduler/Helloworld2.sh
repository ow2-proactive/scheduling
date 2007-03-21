#!/bin/sh

echo
echo --- HelloWorld 2----------------------------------------------

  SCHEDULER_URL=$3
  TASK=$1
  SLEEP=$2


workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.examples.scheduler.HelloWorld2 $TASK $SLEEP $SCHEDULER_URL

echo

