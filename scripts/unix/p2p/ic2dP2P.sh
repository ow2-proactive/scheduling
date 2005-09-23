#!/bin/sh

echo
echo --- IC2D ---------------------------------------------

workingDir=`dirname $0`

cd $PWD/..
. env.sh
cd $workingDir
$JAVACMD -Dproactive.ic2d.hidep2pnode=true org.objectweb.proactive.ic2d.IC2D

echo
echo ------------------------------------------------------------
