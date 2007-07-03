#!/bin/sh

echo
echo --- Jacobi : nodes initialization -----------------------

workingDir=`dirname $0`
. $workingDir/env.sh
export XMLDESCRIPTOR=$workingDir/../../../descriptors/jacobi-nina-sec.xml
$JAVACMD -Xmx900m org.objectweb.proactive.ext.security.test.jacobi.Jacobi $XMLDESCRIPTOR

echo
echo ---------------------------------------------------------
