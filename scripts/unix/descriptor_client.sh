#!/bin/sh

echo
echo --- MiniDescrClient ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.descriptor.MiniDescrClient

echo
echo ---------------------------------------------------------
