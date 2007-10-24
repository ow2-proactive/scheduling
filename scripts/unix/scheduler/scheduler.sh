#!/bin/sh

echo
echo --- Scheduler----------------------------------------------


  SCHEDULER_URL=$2
	RM=$1

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

CLASSPATH=$workingDir/../../scheduler-plugins-src/org.objectweb.proactive.scheduler.plugin/bin/:$CLASSPATH

echo $JAVACMD

yjp=-agentlib:yjpagent

$JAVACMD -Xmx512m -Xms512m org.objectweb.proactive.extra.scheduler.examples.LocalSchedulerExample $RM $SCHEDULER_URL

echo

