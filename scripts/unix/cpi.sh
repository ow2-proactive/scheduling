#!/bin/sh

echo
echo --- MPI deployment example ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
PROACTIVE=$workingDir/../..
XMLDESCRIPTOR=$PROACTIVE/descriptors/MPIRemote-descriptor.xml
#$JAVACMD -classpath $CLASSPATH    -Dlog4j.configuration=file:/user/smariani/home/ProActive/compile/proactive-log4j -Dproactive.rmi.port=6099 -Dproactive.configuration=/user/smariani/home/ProActiveConfiguration.xml org.objectweb.proactive.examples.mpi.Cpi  $XMLDESCRIPTOR


$JAVACMD -classpath $CLASSPATH -Dproactive.rmi.port=6099 -Dproactive.configuration=/user/smariani/home/ProActiveConfiguration.xml org.objectweb.proactive.examples.mpi.Cpi  $XMLDESCRIPTOR 


echo
echo ------------------------------------------------------------
