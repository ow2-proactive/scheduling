#!/bin/sh

echo
echo --- Bounded Buffer ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.boundedbuffer.AppletBuffer

echo
echo ---------------------------------------------------------
