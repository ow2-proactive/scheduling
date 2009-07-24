#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh rm-log4j-server

opt="-Xms128m -Xmx2048m"
eval $JAVACMD $opt -Dproactive.configuration=$PA_SCHEDULER/config/proactive/ProActiveConfigurationStartRM.xml  org.ow2.proactive.resourcemanager.utils.RMStarter $@

echo
