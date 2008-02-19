#!/bin/sh

echo
echo --- LAUNCHER ----------------------------------------------



workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

echo $JAVACMD

$JAVACMD org.objectweb.proactive.extensions.scheduler.examples.JobLauncher $@

echo

