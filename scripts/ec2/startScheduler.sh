#!/bin/sh

CLASSPATH=.
workingDir="$(dirname $0)/../../bin/unix/"
. $workingDir/env.sh scheduler-log4j-server

yjp=-agentlib:yjpagent
opt="-Xms128m -Xmx2048m"

IP_ADDRESS=$(curl http://whatismyip.com/automation/n09230945NL.asp 2>/dev/null)

eval $JAVACMD -Dderby.stream.error.file=\"$PA_SCHEDULER/.logs/derby.log\" $opt \
    -Dproactive.configuration=$(dirname $0)/data/ProActiveConfigurationStartScheduler.xml  \
    -Dproactive.http.jetty.xml=$(dirname $0)/data/jetty.xml \
    org.ow2.proactive.scheduler.util.SchedulerStarter -u http://$IP_ADDRESS:8095/

echo
