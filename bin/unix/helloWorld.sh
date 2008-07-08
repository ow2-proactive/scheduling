#!/bin/sh

echo
echo --- HelloWorld----------------------------------------------

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh


echo $JAVACMD

$JAVACMD org.ow2.proactive.scheduler.examples.SimpleHelloWorld

echo

