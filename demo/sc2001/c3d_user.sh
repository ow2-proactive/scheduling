#!/bin/sh

echo
echo --- C3D User ---------------------------------------------

if [ $# -ne 1 ]
then
echo "
 syntax:

   ./c3d_user.sh local
     - launch one user on the local node ///<HOSTNAME>UserNode
       The node IS created.

   ./c3d_user.sh <host index>
     - launch one user on the node 2 on the host given by the index
       The node IS NOT created

    We assume that 2*n ProActive nodes have been launched
    on n bi-pro hosts (one node per processor per host).
    On each host one node is named <hostname>Node1 and the
    other <hostname>Node2.
    In parameter you have to pass the number of the host 
    the user has to be created on. The user 
    is always created on the node n2 on that host number.

    ex : ./c3d_user.sh 2

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
    targetNode=///${HOSTNAME}UserNode
    $JAVACMD org.objectweb.proactive.rmi.StartNode $targetNode &
    sleep 5
fi

$JAVACMD org.objectweb.proactive.examples.c3d.C3DUser $targetNode

killall java

echo
echo ---------------------------------------------------------
