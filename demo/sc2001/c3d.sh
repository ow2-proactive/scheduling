#!/bin/sh

echo
echo --- C3D ---------------------------------------------

if [ $# -ne 1 ]
then
echo "
 syntax:

   ./c3d.sh local
     - launch one dispatcher on the local node ///<HOSTNAME>Node
       The node IS created.

   ./c3d.sh <host index>
     - launch one dispatcher on the node 2 on the host given by the index
       The node IS NOT created

    We assume that 2*n ProActive nodes have been launched
    on n bi-pro hosts (one node per processor per host).
    On each host one node is named <hostname>Node1 and the
    other <hostname>Node2.
    In parameter you have to pass the number of the host 
    the dispatcher has to be created on. The dispatcher 
    is always created on the node n2 on that host number.

    ex : ./c3d.sh 2

    "
    exit 1
fi

workingDir=`dirname $0`
. $workingDir/env.sh

if [ $1 != "local" ]
then
    targetHost=`cat $workingDir/hostsList | cut -f $1 -d " "`
    targetNode=//${targetHost}/${targetHost}Node2
else
    targetNode=///${HOSTNAME}Node
    $JAVACMD org.objectweb.proactive.StartNode $targetNode &
    sleep 5
fi

$JAVACMD org.objectweb.proactive.examples.c3d.C3DDispatcher nodesList $targetNode

killall java

echo
echo ---------------------------------------------------------
