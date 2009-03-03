#!/bin/sh
echo
echo --- StartNode -------------------------------------------

if [ $# -lt 1 ]; then
    echo "
       Start a new Node
         startNode.sh <the url of the node to create>

         ex : startNode.sh  rmi://$HOSTNAME/node1
         ex : startNode.sh jini://$HOSTNAME/node2

    "
    exit 1
fi

workingDir=`dirname $0`
. $workingDir/env.sh cli
$JAVACMD org.objectweb.proactive.core.node.StartNode "$@"

echo
echo ---------------------------------------------------------
