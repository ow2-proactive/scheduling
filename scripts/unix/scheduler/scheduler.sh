#!/bin/sh

echo
echo --- Scheduler----------------------------------------------

	RM=$1

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

CLASSPATH=$workingDir/../../scheduler-plugins-src/org.objectweb.proactive.scheduler.plugin/bin/:$CLASSPATH

yjp=-agentlib:yjpagent

$JAVACMD -Xmx512m -Xms512m org.objectweb.proactive.extensions.scheduler.examples.LocalSchedulerExample $RM

echo

