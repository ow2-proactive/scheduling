#!/bin/sh
echo
echo --- StartNode -------------------------------------------

if [ $# -lt 1 ]; then
    echo "
       Start a new Node
         startNode.sh <the url of the node to create>
  
         ex : startNode.sh  rmi://$HOSTNAME/node1
         ex : startNode.sh http://$HOSTNAME/node2
 
	 Node started with a random name !!!
    "
    
fi

workingDir=`dirname $0`
. $workingDir/../ProActive/scripts/unix/env.sh
$JAVACMD -Dproactive.runtime.name=PA_JVM1 org.objectweb.proactive.StartNode $1 $2 $3 $4 $5 $6 $7

echo
echo ---------------------------------------------------------
