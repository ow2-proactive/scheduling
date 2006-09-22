#!/bin/sh

echo
echo --- Scilab GUI ---------------------------------------------



if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh
. $PROACTIVE/scripts/unix/scilab/scilab_env.sh

$JAVACMD org.objectweb.proactive.ext.scilab.gui.SciFrame

echo
echo ------------------------------------------------------------
