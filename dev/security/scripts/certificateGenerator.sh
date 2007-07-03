#!/bin/sh

echo
echo --- ProActive Certificate Generator -----------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD org.objectweb.proactive.ext.security.gui.ProActiveCertificateGenerator

echo
echo ---------------------------------------------------------
