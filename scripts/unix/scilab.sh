#!/bin/sh

echo
echo --- Scilab example ---------------------------------------------

. ~/scilab/build4/scilab/env.sh

PROACTIVE=/user/amangin/home/ProActive  
. $PROACTIVE/scripts/unix/env.sh


echo $SCI
$JAVACMD org.objectweb.proactive.ext.scilab.$1

echo
echo ------------------------------------------------------------
