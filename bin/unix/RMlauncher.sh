#!/bin/sh

echo
echo --- RESOURCE MANAGER - LAUNCHER ----------------------------------------------

workingDir=`pwd`
CLASSPATH=.
. ./env.sh

opt="-Xms128m -Xmx2048m"

$JAVACMD $opt org.ow2.proactive.resourcemanager.utils.RMLauncher $@

echo
