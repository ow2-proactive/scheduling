#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh scheduler-log4j-server

yjp=-agentlib:yjpagent
opt="-Xms128m -Xmx2048m"

eval $JAVACMD -Dderby.stream.error.file=\"$PA_SCHEDULER/.logs/derby.log\" $opt -Dproactive.configuration=$PA_SCHEDULER/config/proactive/ProActiveConfigurationStartScheduler.xml  org.ow2.proactive.scheduler.util.SchedulerStarter $@

echo
