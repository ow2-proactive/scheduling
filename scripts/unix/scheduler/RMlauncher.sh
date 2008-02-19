#!/bin/sh

echo
echo --- RESOURCE MANAGER - LAUNCHER ----------------------------------------------

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

opt="-Xms128m -Xmx2048m"

$JAVACMD $opt org.objectweb.proactive.extensions.resourcemanager.test.util.RMLauncher $@

echo

