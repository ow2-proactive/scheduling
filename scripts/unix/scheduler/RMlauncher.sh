#!/bin/sh

echo
echo --- RESOURCE MANAGER - LAUNCHER ----------------------------------------------

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD -Xms128m -Xmx2048m org.objectweb.proactive.extensions.resourcemanager.test.util.RMLauncher $@

echo

