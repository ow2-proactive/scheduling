#!/bin/sh

echo
echo --- C3D ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.StartNode //localhost/Renderer1 &
$JAVACMD org.objectweb.proactive.StartNode //localhost/Renderer2 &
$JAVACMD org.objectweb.proactive.StartNode //localhost/Renderer3 &
$JAVACMD org.objectweb.proactive.StartNode //localhost/Renderer4 &
sleep 4

$JAVACMD org.objectweb.proactive.examples.c3d.C3DDispatcher c3d.hosts

killall java

echo
echo ---------------------------------------------------------
