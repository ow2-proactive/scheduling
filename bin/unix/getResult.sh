#!/bin/sh

echo
echo --- DemoGetResult ----------------------------------------------

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh



$JAVACMD org.ow2.proactive.scheduler.examples.GetJobResult $@

echo

