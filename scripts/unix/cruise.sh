#!/bin/sh

echo
echo --- Cruise Control ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.cruisecontrol.CruiseControlApplet

echo
echo ---------------------------------------------------------
