#!/bin/sh

echo
echo --- Penguin ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
export XMLDESCRIPTOR=$workingDir/../../descriptors/Penguin.xml
$JAVACMD org.objectweb.proactive.examples.penguin.PenguinControler $XMLDESCRIPTOR

echo
echo ------------------------------------------------------------
