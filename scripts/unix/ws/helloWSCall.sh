#!/bin/sh

echo
echo --- Hello World Web Service Call ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/envWS.sh

$JAVACMD org.objectweb.proactive.examples.webservices.helloWorld.WSClient

echo
echo ------------------------------------------------------------
