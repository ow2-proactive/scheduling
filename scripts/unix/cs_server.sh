#!/bin/sh

echo
echo --- ClientServer : Server ---------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.cs.Server

echo
echo ---------------------------------------------------------
