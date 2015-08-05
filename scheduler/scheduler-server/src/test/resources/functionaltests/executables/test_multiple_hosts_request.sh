#!/bin/sh

PA_CORE_NB=$1
PA_NODEFILE=$2

TEST_RES=0

if [ -z "$PA_CORE_NB" ]
then
	echo "Error 'PA_CORE_NB' env variable is not defined"
	TEST_RES=1
fi

if [ "$PA_CORE_NB" != "$1" ]
then
	echo "Error : number of booked host is not $1"
	TEST_RES=1
fi

if [ -z "$PA_NODEFILE" ]
then
	echo "Error 'PA_NODEFILE' env variable is not defined"
	TEST_RES=1
fi

if [ ! -r $PA_NODEFILE ]
then
	echo "Error cannot read 'PA_NODEFILE'"
	TEST_RES=1
else
	NBS_LINES=`wc -l < $PA_NODEFILE | tr -d ' '`
	if [ "$NBS_LINES" != "$1" ]
	then
		echo "Error 'PA_NODEFILE' must have $1 lines, it has $NBS_LINES"
		cat $PA_NODEFILE
		TEST_RES=1
	fi
fi

exit $TEST_RES
