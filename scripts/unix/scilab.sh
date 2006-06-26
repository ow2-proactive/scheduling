#!/bin/sh

echo
echo --- Scilab example ---------------------------------------------

. ~amangin/scilab/build4/scilab/env.sh

workingDir=`dirname $0`
. $workingDir/env.sh
PROACTIVE=$workingDir/../..


$JAVACMD org.objectweb.proactive.ext.scilab.$1

echo
echo ------------------------------------------------------------
