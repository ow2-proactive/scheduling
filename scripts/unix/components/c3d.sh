#!/bin/sh

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi

. $PROACTIVE/scripts/unix/env.sh

JAVACMD=$JAVACMD" -Dfractal.provider=org.objectweb.proactive.core.component.Fractive"

if [ "$1" = "" ]
then 
  DESC=../../../descriptors/components/C3D_all.xml
else
  DESC=$1
fi

$JAVACMD org.objectweb.proactive.examples.components.c3d.Main $DESC