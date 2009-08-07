#!/bin/sh
echo number of cores : $PAS_CORE_NB
echo nodes file path : $PAS_NODEFILE
workingDir=`dirname $0`
mpirun -np $PAS_CORE_NB --hostfile $PAS_NODEFILE $workingDir/mpiTest
