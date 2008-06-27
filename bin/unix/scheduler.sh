#!/bin/sh

echo
echo --- Scheduler----------------------------------------------


CLASSPATH=.

workingDir=`pwd`
. ./env.sh

CLASSPATH=$workingDir/../../scheduler-plugins-src/org.objectweb.proactive.scheduler.plugin/bin/:$CLASSPATH

yjp=-agentlib:yjpagent
opt="-Xms128m -Xmx2048m"


$JAVACMD org.ow2.proactive.scheduler.examples.LocalSchedulerExample $@

echo

