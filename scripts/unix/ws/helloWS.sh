#!/bin/sh

echo
echo --- Hello World Web Service ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/envWS.sh



$JAVACMD org.objectweb.proactive.examples.webservices.helloWorld.HelloWorld

echo
echo ------------------------------------------------------------
