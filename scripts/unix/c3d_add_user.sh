#!/bin/sh

echo
echo --- C3D ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.examples.c3d.C3DUser

killall java

echo
echo ---------------------------------------------------------
