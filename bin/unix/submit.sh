#!/bin/sh

echo
echo ---SCHEDULER JOB LAUNCHER ----------------------------------------------

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh


$JAVACMD org.ow2.proactive.scheduler.examples.JobLauncher $@

echo

