#! /bin/sh

echo 'Starting Fault-Tolerant version of ProActive NBody...'

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/nbody.sh -3dft $1 $2