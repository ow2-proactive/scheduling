#!/bin/sh

echo
echo "--- Reader / Writer ---------------------------------------------"

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.readers.AppletReader

echo
echo ------------------------------------------------------------
