#!/bin/sh

echo
echo --- N-body with ProActive ---------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.examples.nbody.Start $1 $2 $3 $4

echo
echo ---------------------------------------------------------
