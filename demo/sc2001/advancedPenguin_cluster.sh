#! /usr/bin/gnu/bash
echo
echo --- PENGUIN ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.penguin.AdvancedPenguinControler `cat nodesList`

echo
echo ------------------------------------------------------------
