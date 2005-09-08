#!/bin/sh

echo
echo --- Hello World example ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh
PROACTIVE=$workingDir/../..
echo -n "Do you want to use a Local or remote descriptor file ? Simplest is local [L/R] "
read ans
if [ $ans = "R" -o $ans = "r" ]
then 
  XMLDESCRIPTOR=$PROACTIVE/descriptors/helloRemote.xml
else
  XMLDESCRIPTOR=$PROACTIVE/descriptors/helloLocal.xml
fi
$JAVACMD org.objectweb.proactive.examples.hello.Hello $XMLDESCRIPTOR

echo
echo ------------------------------------------------------------
