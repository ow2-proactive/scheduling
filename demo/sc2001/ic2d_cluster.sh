#!/bin/sh

echo
echo --- IC2D ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.ic2d.IC2D hostsList

echo
echo ------------------------------------------------------------
