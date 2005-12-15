#!/bin/sh

echo
echo --- MPI deployment example ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
PROACTIVE=$workingDir/../..
XMLDESCRIPTOR=$PROACTIVE/descriptors/MPI-descriptor.xml
$JAVACMD org.objectweb.proactive.examples.mpi.Cpi 5  $XMLDESCRIPTOR

echo
echo ------------------------------------------------------------
