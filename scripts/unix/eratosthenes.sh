#!/bin/sh

echo
echo --- Eratosthenes ---------------------------------------------
echo You may pass an XML Deployment Descriptor file as first parameter
echo An example can be found in ProActive/descriptors/Eratosthenes.xml

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.examples.eratosthenes.Main $@

echo
echo ------------------------------------------------------------
