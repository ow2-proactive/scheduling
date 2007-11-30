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

$JAVACMD -Djava.library.path=$LD_LIBRARY_PATH org.objectweb.proactive.extensions.scilab.gui.MSFrame

echo
echo ------------------------------------------------------------
