#! /bin/sh

workingDir=`dirname $0`
. $workingDir/env.sh router

VM_ARGS="-Xmx512m -Xms512m -server"

eval $JAVACMD $VM_ARGS org.objectweb.proactive.extra.messagerouting.router.Main "$@"
