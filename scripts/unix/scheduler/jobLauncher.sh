#!/bin/sh

echo
echo --- LAUNCHER ----------------------------------------------



workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

echo $JAVACMD

$JAVACMD org.objectweb.proactive.extra.scheduler.examples.JobLauncher $1 $2 $3 $4

echo

