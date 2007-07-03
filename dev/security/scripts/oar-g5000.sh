#!/bin/sh

echo
echo --- G5000 oar : nodes initialization -----------------------

workingDir=`dirname $0`
. $workingDir/env.sh
export XMLDESCRIPTOR=$workingDir/../descriptor/oar-g5000.xml
$JAVACMD -Xmx900m org.objectweb.proactive.ext.security.test.TestOAR $XMLDESCRIPTOR

echo
echo ---------------------------------------------------------
