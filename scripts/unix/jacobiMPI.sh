#!/bin/sh

echo
echo --- MPI deployment example ---------------------------------------------

echo " --- RUNNING JACOBI ON LOCALHOST ---"

MPICC=$(which mpicc 2> /dev/null || echo "mpicc Not Found!" >&2 )
[[ -z $MPICC ]] && exit 1
workingDir=`dirname $0`
. $workingDir/env.sh
EXAMPLES=$PROACTIVE/src/Examples/org/objectweb/proactive/examples/mpi

echo "Compiling source file..."
$MPICC $EXAMPLES/jacobi.c -lm -o $EXAMPLES/jacobi

if [ -f /usr/bin/lamboot ]
then 
/usr/bin/lamboot
else 
echo "ERROR: you need \"lamboot\" to start a Local Area Multicomputer simulator"	
fi

XMLDESCRIPTOR=$PROACTIVE/descriptors/MPI-descriptor.xml

$JAVACMD -Dlog4j.configuration=file:$PROACTIVE/scripts/proactive-log4j -Dproactive.rmi.port=6099 org.objectweb.proactive.examples.mpi.Jacobi  $XMLDESCRIPTOR 

echo "Killing lam daemon..."
killall lamd

echo
echo ------------------------------------------------------------
