#!/bin/sh

CLASSPATH=.
workingDir="$(dirname $0)/../../bin/unix/"
. $workingDir/env.sh rm-log4j-server

opt="-Xms128m -Xmx2048m"
eval $JAVACMD $opt \
    -Dproactive.configuration=$(dirname $0)/data/ProActiveConfigurationStartRM.xml \
    -Dproactive.http.jetty.xml=$(dirname $0)/data/jetty.xml \
    org.ow2.proactive.resourcemanager.utils.RMStarter $@

echo
