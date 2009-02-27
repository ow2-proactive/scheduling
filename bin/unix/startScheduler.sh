#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh

yjp=-agentlib:yjpagent
opt="-Xms128m -Xmx2048m"

$JAVACMD $opt org.ow2.proactive.scheduler.examples.SchedulerStarter $@

echo

