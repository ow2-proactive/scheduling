#!/bin/sh

echo
echo --- C3D ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.rmi.StartNode //localhost/users &
sleep 5

$JAVACMD org.objectweb.proactive.examples.c3d.C3DUser //localhost/users

killall java

echo
echo ---------------------------------------------------------
