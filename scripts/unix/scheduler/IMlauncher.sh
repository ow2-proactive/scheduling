#!/bin/sh

echo
echo --- INFRASTRUCTURE MANAGER - LAUNCHER ----------------------------------------------

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.extra.infrastructuremanager.test.util.IMLauncher

echo

