#!/bin/sh

echo
echo --- N-body with ProActive ---------------------------------

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh

if [ "$1" = "-displayft" -o "$1" = "-3dft" ]
then
echo ' **WARNING** : $PROACTIVE/descriptors/FaultTolerantWorkers.xml MUST BE SET WITH EXISTING HOSTNAMES !'
export XMLDESCRIPTOR=$PROACTIVE/descriptors/FaultTolerantWorkers.xml
else
export XMLDESCRIPTOR=$PROACTIVE/descriptors/Workers.xml
fi


$JAVACMD org.objectweb.proactive.examples.nbody.common.Start $XMLDESCRIPTOR $1 $2 $3

echo
echo ---------------------------------------------------------
