#!/bin/sh

echo
echo --- ClientServer : Client---------------------------------

if [ $# -lt 1 ]; then
    echo "

 Launch one client
    cs_client.sh <name of the client on the localhost> [(optional) <hostname of the server (localhost by default)>]
 
     ex : cs_client client1
          cs_client client1 sabini
 
    "
    exit 1
fi

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.cs.Client $@

echo
echo ---------------------------------------------------------
