#!/bin/sh

echo
echo --- ClientServer : Server ---------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n  org.objectweb.proactive.examples.hello2.Server

echo
echo ---------------------------------------------------------
