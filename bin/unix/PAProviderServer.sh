#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh

eval $JAVACMD org.objectweb.proactive.extensions.vfsprovider.console.PAProviderServerStarter $@

echo

