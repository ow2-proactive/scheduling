#!/bin/sh

echo
echo --- Matrix : nodes initialization -----------------------

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh
export XMLDESCRIPTOR=$workingDir/../../../descriptors/MatrixApplication.xml
$JAVACMD org.objectweb.proactive.examples.matrix.Main 300 $XMLDESCRIPTOR

echo
echo ---------------------------------------------------------
