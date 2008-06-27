#!/bin/sh

echo
echo --- DemoGetResult ----------------------------------------------

CLASSPATH=.
workingDir=`pwd`
. ./env.sh



$JAVACMD org.ow2.proactive.scheduler.examples.GetJobResult $@

echo

