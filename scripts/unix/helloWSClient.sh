#!/bin/sh

echo
echo --- ClientServer : Server ---------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.ext.webservices.soap.test.TestSoap

echo
echo ---------------------------------------------------------
