#!/bin/sh

echo
echo --- Jacobi : nodes initialization -----------------------

workingDir=`dirname $0`
. $workingDir/env.sh
export XMLDESCRIPTOR=$workingDir/../descriptor/jacobi-16-sec-grid5000-sophia.xml
$JAVACMD -Xmx600m org.objectweb.proactive.ext.security.test.jacobi.Jacobi $XMLDESCRIPTOR

echo
echo ---------------------------------------------------------
