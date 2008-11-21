#!/bin/sh

rm -rf SCHEDULER_DB
./createDataBase.sh ../../config/database/scheduler_db.cfg

echo
echo --- Scheduler----------------------------------------------


workingDir=`pwd`
CLASSPATH=.
. ./env.sh


yjp=-agentlib:yjpagent
opt="-Xms128m -Xmx512m"


$JAVACMD $opt org.ow2.proactive.scheduler.examples.SchedulerStarter $@

echo

