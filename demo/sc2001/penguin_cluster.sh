#! /usr/bin/gnu/bash
echo
echo --- PENGUIN ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.penguin.Penguin `cat nodesList`

echo
echo ------------------------------------------------------------
