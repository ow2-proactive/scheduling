#!/bin/sh

echo
echo --- Matrix : nodes initialization -----------------------

workingDir=`dirname $0`
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.StartNode //localhost/Renderer1 &
$JAVACMD org.objectweb.proactive.StartNode //localhost/Renderer2 &
$JAVACMD org.objectweb.proactive.StartNode //localhost/Renderer3 &
$JAVACMD org.objectweb.proactive.StartNode //localhost/Renderer4 &
sleep 6

$JAVACMD org.objectweb.proactive.examples.matrix.Main c3d.hosts 300

killall java

echo
echo ---------------------------------------------------------
