#!/bin/sh

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi

. $PROACTIVE/scripts/unix/env.sh

JAVACMD=$JAVACMD" -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"


echo --- GCM application: tutorial ---------------------
$JAVACMD -Dproactive.hostname=138.96.20.214 org.objectweb.proactive.examples.components.userguide.Main $*
echo ---------------------------------------------------------
