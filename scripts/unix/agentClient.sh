#!/bin/sh
echo
echo --- upperClient -------------------------------------------

if [ $# -lt 1 ]; then
    echo "
       Start AgentClient
         AgentClient.sh <the url of the node where Active Object Agent is going to be migrated to>
  
         ex : agentClient.sh  //$HOSTNAME/node1
         The node has to be previously launched with the command ./startNode.sh //$HOSTNAME/node1
 
    "
    exit 1
fi

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.migration.AgentClient $1 $2 $3 $4 $5 $6 $7

echo
echo ---------------------------------------------------------
