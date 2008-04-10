#!/bin/sh

echo
echo --- Flowshop ---------------------------------------------

if [ $# -ge 2 ] 
then
	_bench=$1
	_desc=$2
	shift 2
elif [ $# -eq 0 ] 
then
	_bench=../../src/Examples/org/objectweb/proactive/examples/flowshop/taillard/test_10_10.txt
	_desc=../../descriptors/WorkersApplication.xml
else 
	echo Usage: $0 "[<bench_file> <descriptors_location>]"
	echo
	exit
fi

workingDir=`dirname $0`
. $workingDir/env.sh

$JAVACMD -Dflowshopparser.taillard=false org.objectweb.proactive.examples.flowshop.Main -bench $_bench -desc $_desc "$@"

echo
echo ------------------------------------------------------------
