#!/bin/sh

echo
echo --- Penguin ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.StartNode //localhost/one &
$JAVACMD org.objectweb.proactive.StartNode //localhost/two &
$JAVACMD org.objectweb.proactive.examples.penguin.PenguinControler //localhost/one //localhost/two

killall java

echo
echo ------------------------------------------------------------
