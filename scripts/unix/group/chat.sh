#!/bin/sh

echo
echo --- Chat with ProActive ---------------------------------

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh

$JAVACMD org.objectweb.proactive.examples.chat.Chat "$@"

echo
echo ---------------------------------------------------------
