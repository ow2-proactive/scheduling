#!/bin/sh

echo
echo --- ProFractal ---------------------------------------------

if [ ! $PROFRACTALIB ]
then
workingDir=`dirname $0`
PROFRACTALIB=$workingDir/../.././src/org/objectweb/proactive/examples/profractal/lib
fi

workingDir=`dirname $0`
. $workingDir/env.sh
CLASSPATH=$CLASSPATH:$PROFRACTALIB/jai_imageio.jar:$PROFRACTALIB/mlibwrapper_jai.jar:$PROFRACTALIB/clibwrapper_jiio.jar:$PROFRACTALIB/jai_codec.jar:$PROFRACTALIB/jai_core.jar
$JAVACMD org.objectweb.proactive.examples.fractParallel profractalrsrcs

echo
echo ------------------------------------------------------------
