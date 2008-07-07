#!/bin/sh

echo
echo --- HelloWorld----------------------------------------------

workingDir=`pwd`
CLASSPATH=.
. ./env.sh


echo $JAVACMD

$JAVACMD org.ow2.proactive.scheduler.examples.SimpleHelloWorld

echo

