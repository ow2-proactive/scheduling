#!/bin/sh

echo
echo --- MiniDescrServer ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.descriptor.MiniDescrServer

echo
echo ---------------------------------------------------------
