#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh client

$JAVACMD org.ow2.proactive.scheduler.common.util.userconsole.UserShell $@

echo
