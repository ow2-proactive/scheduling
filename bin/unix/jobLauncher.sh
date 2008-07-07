#!/bin/sh

echo
echo ---SCHEDULER JOB LAUNCHER ----------------------------------------------

workingDir=`pwd`
CLASSPATH=.
. ./env.sh


$JAVACMD org.ow2.proactive.scheduler.examples.JobLauncher $@

echo

