#!/bin/sh
echo
echo --- upperClient -------------------------------------------

if [ $# -lt 1 ]; then
    echo "
       Start upperClient
         upperClient.sh <the url of the node where Active Object Upper is going to be migrated to>
  
         ex : upperClient.sh  //$HOSTNAME/node1
         The node has to be previously launched with the command ./startNode.sh //$HOSTNAME/node1
 
    "
    exit 1
fi

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.upper.UpperClient $1 $2 $3 $4 $5 $6 $7

echo
echo ---------------------------------------------------------
