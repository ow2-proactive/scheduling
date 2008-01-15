#!/bin/sh

echo
echo --- DemoGetResult ----------------------------------------------


workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.extensions.scheduler.examples.GetJobResult $1 $2 $3

echo

