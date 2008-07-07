#!/bin/sh

echo
echo --- DemoGetResult ----------------------------------------------

workingDir=`pwd`
CLASSPATH=.
. ./env.sh



$JAVACMD org.ow2.proactive.scheduler.examples.GetJobResult $@

echo

