#!/bin/sh

echo
echo --- Fibonacci ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.fibonacci.Add

echo
echo ------------------------------------------------------------
