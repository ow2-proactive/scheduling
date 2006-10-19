#!/bin/sh

echo
echo --- TimIt --------------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
CONFIG=$workingDir/../../src/org/objectweb/proactive/examples/timit
$JAVACMD org.objectweb.proactive.benchmarks.timit.TimIt -c $CONFIG/config.xml

echo
echo ------------------------------------------------------------
