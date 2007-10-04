#!/bin/sh

echo
echo --- HelloWorld----------------------------------------------


workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

echo $JAVACMD

$JAVACMD org.objectweb.proactive.extra.scheduler.examples.SimpleHelloWorld

echo

