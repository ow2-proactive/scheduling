#!/bin/sh

if [ -d SCHEDULER_DB ]
then
rm -rf SCHEDULER_DB
rm derby.log
fi


workingDir=`pwd`
CLASSPATH=.
. ./env.sh


yjp=-agentlib:yjpagent
opt="-Xms128m -Xmx512m"

$JAVACMD $opt org.ow2.proactive.scheduler.examples.SchedulerStarter $@

echo

