#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh client

opt="-Xms128m -Xmx2048m"

$JAVACMD org.ow2.proactive.resourcemanager.utils.AdminRM $@

echo
