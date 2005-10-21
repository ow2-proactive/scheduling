#!/bin/sh

echo
echo --- ProActive TryWithCatch annotator -----------------------

workingDir=`dirname $0`
. $workingDir/env.sh

$JAVACMD trywithcatch.TryWithCatch "$@"
