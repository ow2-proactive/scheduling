#!/bin/sh

echo
echo --- Collaborative ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.rmi.StartNode //localhost/home &
$JAVACMD org.objectweb.proactive.rmi.StartNode //localhost/one &
$JAVACMD org.objectweb.proactive.rmi.StartNode //localhost/two &
$JAVACMD org.objectweb.proactive.rmi.StartNode //localhost/three &
sleep 5

$JAVACMD org.objectweb.proactive.examples.collaborative.Agent 3 //localhost/one //localhost/two //localhost/three

killall java

echo
echo ---------------------------------------------------------
