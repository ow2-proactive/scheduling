#!/bin/sh

echo
echo --- Philosophers ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.philosophers.AppletPhil

echo
echo ------------------------------------------------------------
