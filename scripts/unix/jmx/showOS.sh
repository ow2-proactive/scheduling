#!/bin/sh

echo
echo --- JMX Show OS ---------------------------------------------

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh



$JAVACMD org.objectweb.proactive.examples.jmx.ShowOS

echo
echo ------------------------------------------------------------
