#!/bin/sh

echo
echo --- Hello World ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.hello.HelloApplet

echo
echo ------------------------------------------------------------
