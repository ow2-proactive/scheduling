#!/bin/sh

echo
echo --- Penguin ---------------------------------------------

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh
export XMLDESCRIPTOR=$workingDir/../../descriptors/Penguin.xml
$JAVACMD org.objectweb.proactive.examples.penguin.PenguinControler $XMLDESCRIPTOR

echo
echo ------------------------------------------------------------
