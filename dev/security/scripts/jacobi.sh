#!/bin/sh

echo
echo --- Jacobi : nodes initialization -----------------------

workingDir=`dirname $0`
. $workingDir/env.sh
export XMLDESCRIPTOR=$workingDir/../descriptor/jacobi-16-grid5000-sophia.xml
$JAVACMD -Xmx900m org.objectweb.proactive.core.security.test.jacobi.Jacobi $XMLDESCRIPTOR

echo
echo ---------------------------------------------------------
