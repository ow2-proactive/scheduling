#!/bin/sh

echo
echo --- Flowshop ---------------------------------------------

echo Usage: $0 bench_file descriptors.xml
echo
_bench=$1
_desc=$2
shift
shift

workingDir=`dirname $0`
. $workingDir/env.sh

$JAVACMD -Dflowshopparser.taillard=false org.objectweb.proactive.examples.flowshop.Main -bench $_bench -desc $_desc $*

echo
echo ------------------------------------------------------------
