#!/bin/sh

echo
echo --- RESOURCE MANAGER - LAUNCHER ----------------------------------------------

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD -Xmx2048m -Xms2048m org.objectweb.proactive.extensions.resourcemanager.test.util.RMLauncher $@

echo

