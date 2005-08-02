#!/bin/sh

echo
echo --- Pi -----------------------

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh
export XMLDESCRIPTOR=$workingDir/../../descriptors/Matrix.xml
time $JAVACMD org.objectweb.proactive.examples.robustarith.Main $XMLDESCRIPTOR

echo
echo ---------------------------------------------------------
