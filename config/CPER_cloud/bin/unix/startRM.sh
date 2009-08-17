#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh rm-log4j-server

opt="-Dproactive.http.port=8084"
# Memory options similar to the AS
mem_opt="-Xms1024m -Xmx2048m  -XX:+AggressiveHeap -XX:NewRatio=2 -XX:SurvivorRatio=16 -XX:MaxPermSize=512m"

# stubz & classes for the RM RCP
CLASSPATH=$CLASSPATH:$PA_SCHEDULER/scheduler_plugins/org.ow2.proactive.resourcemanager.plugin/classes/:$PATH_TO_RM_RCP_GENERATED_STUBZ

eval $JAVACMD $opt $mem_opt org.ow2.proactive.resourcemanager.utils.RMStarter $@

echo
