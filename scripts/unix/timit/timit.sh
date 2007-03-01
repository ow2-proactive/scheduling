#!/bin/sh

echo
echo --- TimIt --------------------------------------------------

workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
. $PROACTIVE/scripts/unix/env.sh

$JAVACMD org.objectweb.proactive.benchmarks.timit.TimIt -c config.xml

echo
echo ------------------------------------------------------------
