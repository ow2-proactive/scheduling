#!/bin/bash

workingDir=`dirname $0`
. $workingDir/../../scripts/unix/env.sh

DIRLIBS=$workingDir/../lib/*.jar
for i in $DIRLIBS
do
  if [ -z "$LOCALCLASSPATH" ] ; then
    LOCALCLASSPATH=$i
  else
    LOCALCLASSPATH="$i":$LOCALCLASSPATH
  fi
done


CLASSPATH=$LOCALCLASSPATH:$CLASSPATH
echo $CLASSPATH

$JAVACMD -classpath "$CLASSPATH"  \
   de.hunsicker.jalopy.plugin.console.ConsolePlugin \
  -c $workingDir/../../compile/proactiveJalopy.xml \
  -d $workingDir/../tmp $@

