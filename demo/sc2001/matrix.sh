#!/bin/sh

echo
echo ---- Matrix Multiplication --------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.matrix.Main nodesList $1

echo
echo -----------------------------------
