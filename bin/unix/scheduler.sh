#!/bin/sh

echo
echo --- Scheduler----------------------------------------------


CLASSPATH=.

workingDir=`pwd`
. ./env.sh

yjp=-agentlib:yjpagent
opt="-Xms128m -Xmx2048m"


$JAVACMD org.ow2.proactive.scheduler.examples.LocalSchedulerExample $@

echo

