#!/bin/sh

echo
echo --- LAUNCHER ----------------------------------------------

  SCHEDULER_URL=$3
  NB_JOB=$2
  JOB_URL=$1


workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

echo $JAVACMD

$JAVACMD org.objectweb.proactive.extra.scheduler.examples.JobLauncher $JOB_URL $NB_JOB $SCHEDULER_URL

echo

