#!/bin/sh

echo
echo --- The Garden ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.core.node.StartNode vm1 &
$JAVACMD org.objectweb.proactive.core.node.StartNode vm2 &
sleep 4

$JAVACMD org.objectweb.proactive.examples.garden.Flower

echo
echo ------------------------------------------------------------
