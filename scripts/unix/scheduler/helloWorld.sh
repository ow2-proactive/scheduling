#!/bin/sh

echo
echo --- HelloWorld----------------------------------------------


workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

echo $JAVACMD

$JAVACMD org.objectweb.proactive.extensions.scheduler.examples.SimpleHelloWorld

echo

