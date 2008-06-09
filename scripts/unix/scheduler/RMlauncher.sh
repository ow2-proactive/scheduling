#!/bin/sh

echo
echo --- RESOURCE MANAGER - LAUNCHER ----------------------------------------------

CLASSPATH=.
workingDirectory=`pwd`
cd ..
. ./env.sh

cd $workingDirectory
opt="-Xms128m -Xmx2048m"

$JAVACMD $opt org.objectweb.proactive.extensions.resourcemanager.utils.RMLauncher $@

echo
