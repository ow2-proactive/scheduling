#!/bin/sh

echo
echo --- Hello World Web Service ---------------------------------------------

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh



$JAVACMD org.objectweb.proactive.examples.webservices.helloWorld.HelloWorld $1

echo
echo ------------------------------------------------------------
