#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh cli

$JAVACMD org.ow2.proactive.scheduler.util.AdminScheduler $@


echo
