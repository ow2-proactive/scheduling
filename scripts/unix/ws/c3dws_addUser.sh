#!/bin/sh

echo
echo --- C3D ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/envWS.sh

export XMLDESCRIPTOR=$workingDir/../../descriptors/C3D_User.xml
$JAVACMD org.objectweb.proactive.examples.webservices.c3dWS.C3DUser $XMLDESCRIPTOR



echo
echo ---------------------------------------------------------
