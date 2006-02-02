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
echo "Compiling source file .."
/usr/bin/mpicc $EXAMPLES/cpi.c -o $EXAMPLES/cpi
fi
if [ -f /usr/bin/lamboot ]
then 
/usr/bin/lamboot
fi

XMLDESCRIPTOR=$PROACTIVE/descriptors/MPI-descriptor.xml

$JAVACMD -classpath $CLASSPATH -Dproactive.rmi.port=6099 org.objectweb.proactive.examples.mpi.Cpi  $XMLDESCRIPTOR 



echo
echo ------------------------------------------------------------
