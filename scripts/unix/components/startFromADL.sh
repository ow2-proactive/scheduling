#!/bin/sh

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi

. $PROACTIVE/scripts/unix/env.sh

JAVACMD=$JAVACMD" -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"

echo --- Fractal ADL Launcher --------------------------------
$JAVACMD org.objectweb.proactive.examples.components.StartFromADL $@
echo ---------------------------------------------------------
