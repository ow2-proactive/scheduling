#!/bin/sh

echo
echo --- HelloWorld----------------------------------------------

CLASSPATH=.

workingDir=`pwd`
. ./env.sh


echo $JAVACMD

$JAVACMD org.ow2.proactive.scheduler.examples.SimpleHelloWorld

echo

