#!/bin/sh
echo
echo --- StartNode -------------------------------------------

if [ $# -lt 1 ]; then
    echo "
       Start a new Node
         startNode.sh <the url of the node to create>
  
         ex : startNode.sh //localhost/node1
 
    "
    exit 1
fi

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.StartNode $1 $2 $3 $4 $5 $6 $7

echo
echo ---------------------------------------------------------
