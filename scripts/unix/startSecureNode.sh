#!/bin/sh

echo
echo --- StartSecureNode -------------------------------------

if [ $# -lt 4 ]; then
    echo "
       Start a new Node
         startSecureNode.sh <the url of the node to create> <public Certificate filename> <private Certificate filename > <PublicKey filename>
  
         ex : startSecureNode.sh //localhost/node1 <publicCertificate> <privateCertificate> <acPublicKey>
 
    "
    exit 1
fi

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.rmi.StartSecureNode $1 $2 $3 $4 $5 $6 $7 $8 $9

echo
echo ---------------------------------------------------------
