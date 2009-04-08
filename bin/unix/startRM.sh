#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh rm-log4j-server

opt="-Xms128m -Xmx2048m"

$JAVACMD $opt org.ow2.proactive.resourcemanager.utils.RMStarter $@

echo
