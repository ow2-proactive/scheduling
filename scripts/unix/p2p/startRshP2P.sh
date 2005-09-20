#!/bin/sh

HOSTFILE=$1
shift
for host in `cat $HOSTFILE`
do
rsh $host "cd $PWD; ./startP2PServiceOneShot.sh $@" &
usleep 100000
done
