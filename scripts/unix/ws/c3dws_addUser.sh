#!/bin/sh

echo
echo --- C3D ---------------------------------------------

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh

export XMLDESCRIPTOR=$workingDir/../../../descriptors/C3D_User.xml
$JAVACMD org.objectweb.proactive.examples.webservices.c3dWS.C3DUser $XMLDESCRIPTOR



echo
echo ---------------------------------------------------------
