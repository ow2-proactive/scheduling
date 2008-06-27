#!/bin/sh

echo
echo ---SCHEDULER JOB LAUNCHER ----------------------------------------------

CLASSPATH=.
workingDir=`pwd`
. ./env.sh


$JAVACMD org.ow2.proactive.scheduler.examples.JobLauncher $@

echo

