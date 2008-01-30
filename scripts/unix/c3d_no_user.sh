#!/bin/sh

echo
echo --- C3D ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
export XMLDESCRIPTOR=$workingDir/../../descriptors/C3D_Dispatcher_Renderer_Application.xml
$JAVACMD  org.objectweb.proactive.examples.c3d.C3DDispatcher $XMLDESCRIPTOR

echo
echo ---------------------------------------------------------
