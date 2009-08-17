#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh scheduler-log4j-server

yjp=-agentlib:yjpagent
opt="-Dproactive.http.port=8083"
# memory options similar to the AS
mem_opt="-Xms1024m -Xmx2048m -XX:+AggressiveHeap -XX:NewRatio=2 -XX:SurvivorRatio=16 -XX:MaxPermSize=512m"
# stubz & classes for the Scheduler RCP
CLASSPATH=$CLASSPATH:$PA_SCHEDULER/scheduler_plugins/org.ow2.proactive.scheduler.plugin/bin/:$PATH_TO_SCHEDULER_RCP_GENERATED_STUBZ
# stubz & classes for other apps go here
CLASSPATH=$CLASSPATH:$PA_SCHEDULER/stubz/

eval $JAVACMD -Dderby.stream.error.file=\"$PA_SCHEDULER/.logs/derby.log\" $opt $mem_opt org.ow2.proactive.scheduler.util.SchedulerStarter $@

echo

