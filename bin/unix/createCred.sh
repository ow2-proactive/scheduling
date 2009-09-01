#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh log4j-client

eval $JAVACMD \
    org.ow2.proactive.authentication.crypto.CreateCredentials $@

echo
