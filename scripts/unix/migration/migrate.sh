#!/bin/sh
echo
echo --- Migrate -------------------------------------------

if [ $# -lt 2 ]; then
    echo "
       Migrate an ActiveObject from Node1 to Node2
         migrate.sh <the url of the source node> <the url of the destination node>
  
         ex : migrate.sh  rmi://$HOSTNAME/Node1 rmi://$HOSTNAME/Node2
         ex : migrate.sh jini://$HOSTNAME/Node1 rmi://$HOSTNAME/Node2
 
    "
    exit 1
fi

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.migration.SimpleObjectMigration $1 $2
echo
echo ---------------------------------------------------------
