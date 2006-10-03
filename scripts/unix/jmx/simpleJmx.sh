#!/bin/sh

echo
echo --- JMC Test client connector---------------------------------------------

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh



$JAVACMD org.objectweb.proactive.examples.jmx.TestClient

echo
echo ------------------------------------------------------------
