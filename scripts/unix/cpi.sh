#!/bin/sh

echo
echo --- MPI deployment example ---------------------------------------------

echo " --- RUNNING CPI ON LOCALHOST ---"

workingDir=`dirname $0`
. $workingDir/env.sh
PROACTIVE=$workingDir/../..
EXAMPLES=$PROACTIVE/src/org/objectweb/proactive/examples/mpi
if [ -f /usr/bin/mpicc ]
then
echo "Compiling source file..."
/usr/bin/mpicc $EXAMPLES/cpi.c -o $EXAMPLES/cpi
else
echo "ERROR: you need \"mpicc\" to compile MPI\C source file"
exit 127
fi
if [ -f /usr/bin/lamboot ]
then 
/usr/bin/lamboot
else 
echo "ERROR: you need \"lamboot\" to start a Local Area Multicomputer simulator"	
fi

XMLDESCRIPTOR=$PROACTIVE/descriptors/MPI-descriptor.xml

$JAVACMD -classpath $CLASSPATH -Dproactive.rmi.port=6099 org.objectweb.proactive.examples.mpi.Cpi  $XMLDESCRIPTOR 

echo "Killing lam daemon..."
killall lamd

echo
echo ------------------------------------------------------------
