#!/bin/sh

echo
echo --- Scheduler----------------------------------------------


CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh

yjp=-agentlib:yjpagent
opt="-Xms128m -Xmx2048m"

#log4j=-Dlog4j.configuration=file:/user/jlscheef/home/workspace/ProActiveScheduling/bin/proactive-log4j

$JAVACMD $opt org.ow2.proactive.scheduler.examples.LocalSchedulerExample $@

echo

