#!/bin/sh

echo
echo --- Distributed Primes With Master-Worker API -------------------

workingDir=`dirname $0`
. $workingDir/env.sh
$JAVACMD active.DistributedPrimesMW 100

echo
echo -----------------------------------------------------------------
