#!/bin/sh

echo
echo --- Hello World tiny example ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
PROACTIVE=$workingDir/../..
$JAVACMD org.objectweb.proactive.examples.hello.TinyHello

echo
echo ------------------------------------------------------------
