#!/bin/sh

echo
echo ---JMX Test Connector---------------------------------------------

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh



$JAVACMD org.objectweb.proactive.examples.jmx.TestServer $1

echo
echo ------------------------------------------------------------
