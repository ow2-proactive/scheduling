#!/bin/sh

echo
echo --- Penguin ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
export XMLDESCRIPTOR=$workingDir/../../descriptors/MigratableAgent.xml
$JAVACMD org.objectweb.proactive.examples.migration.AgentClient $XMLDESCRIPTOR

echo
echo ------------------------------------------------------------
