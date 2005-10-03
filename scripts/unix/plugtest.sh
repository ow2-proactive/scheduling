#!/bin/sh

cheminXML=/afs/cern.ch/user/m/mozonne/public/ProActive/descriptors/examples

echo
echo --- PLUGTEST ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD  org.objectweb.proactive.examples.plugtest.MTest $cheminXML/$1

echo
echo ------------------------------------------------------------
