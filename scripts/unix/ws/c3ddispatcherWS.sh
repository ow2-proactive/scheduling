#!/bin/sh
echo
echo --- Expose Dispatcher as a web service  -------------------------------------------

if [ $# -lt 1 ]; then
    echo "
       Expose an ActiveObject as a web service 
         c3ddispatcherWS.sh <the url where to deploy the object>
           ex : c3ddispatcherWS.sh trinidad:8080
 
    "
    exit 1
fi

workingDir=`dirname $0`
. $workingDir/envWS.sh
export XMLDESCRIPTOR=$workingDir/../../../descriptors/C3D_Dispatcher_Renderer.xml
$JAVACMD org.objectweb.proactive.examples.webservices.c3dWS.C3DDispatcher $XMLDESCRIPTOR $1 $2
echo
echo ---------------------------------------------------------
