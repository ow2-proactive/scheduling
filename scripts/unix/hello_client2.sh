#!/bin/sh

echo
echo --- ClientServer : Client---------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD  org.objectweb.proactive.examples.hello2.HelloClient 

echo
echo ---------------------------------------------------------
