#!/bin/sh

echo
echo --- The Salishan problems : Problem 3 - The Doctor Office -----

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.doctor.Office

echo
echo -----------------------------------------------------------------
