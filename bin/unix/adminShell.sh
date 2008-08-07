#!/bin/sh

echo
echo --- Admin Shell ----------------------------------------------

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh

$JAVACMD org.ow2.proactive.scheduler.examples.AdminShell $@


echo

