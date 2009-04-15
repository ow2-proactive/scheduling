#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh log4j-client

opt="-Xms128m -Xmx2048m"

eval $JAVACMD org.ow2.proactive.resourcemanager.utils.adminconsole.AdminController $@

echo
