#!/bin/sh

workingDir=`dirname $0`

cd $PWD/..
. env.sh
cd $workingDir

HOSTFILE=$1
shift
for host in `cat $HOSTFILE`
do
rsh $host $workingDir/startP2PServiceOneShot.sh "$@" &
usleep 100000
done
