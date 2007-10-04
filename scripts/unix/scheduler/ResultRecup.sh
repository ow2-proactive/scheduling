#!/bin/sh

echo
echo --- DemoResultRecup ----------------------------------------------

  SCHEDULER_URL=$1


workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.extra.scheduler.examples.ResultRecup $SCHEDULER_URL

echo

