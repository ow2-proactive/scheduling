#!/bin/sh

echo
echo --- Jacobi : nodes initialization -----------------------

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh
export XMLDESCRIPTOR=$workingDir/../../../descriptors/Matrix.xml
$JAVACMD org.objectweb.proactive.examples.jacobi.Jacobi $XMLDESCRIPTOR

echo
echo ---------------------------------------------------------
