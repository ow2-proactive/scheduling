#!/bin/sh

echo
echo --- IntegralPi --------------------------------------------------

if [ "$1" = "NULL" ]
then
np=$1
else
np=4
fi

echo The number of workers is $np
echo Feel free to edit this script if you want to change the deployement descriptor.

workingDir=`dirname $0`
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.examples.integralpi.Launcher $workingDir/../../descriptors/Matrix.xml $np  

echo
echo ------------------------------------------------------------
