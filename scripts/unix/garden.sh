#!/bin/sh

echo
echo --- The Garden ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.rmi.StartNode ///vm1 &
$JAVACMD org.objectweb.proactive.rmi.StartNode ///vm2 &
sleep 4

$JAVACMD org.objectweb.proactive.examples.garden.Flower

killall java

echo
echo ------------------------------------------------------------
