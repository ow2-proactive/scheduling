#!/bin/sh

echo
echo --- N-body with ProActive ---------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh

export XMLDESCRIPTOR=$workingDir/../../descriptors/Workers.xml
$JAVACMD org.objectweb.proactive.examples.nbody.common.Start $XMLDESCRIPTOR $1 $2 $3

echo
echo ---------------------------------------------------------
