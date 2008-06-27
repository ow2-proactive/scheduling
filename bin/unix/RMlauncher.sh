#!/bin/sh

echo
echo --- RESOURCE MANAGER - LAUNCHER ----------------------------------------------

CLASSPATH=.
workingDir=`pwd`
. ./env.sh

opt="-Xms128m -Xmx2048m"

$JAVACMD $opt org.ow2.proactive.resourcemanager.utils.RMLauncher $@

echo
