#!/bin/sh

workingDir=`dirname $0`

cd $PWD/..
. env.sh
cd $workingDir

exec nice -19 $JAVACMD org.objectweb.proactive.extra.p2p.daemon.Daemon "$@"
