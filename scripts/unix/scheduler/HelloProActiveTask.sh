#!/bin/sh

echo
echo --- HelloProActiveTask ----------------------------------------------



workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh    

$JAVACMD -Dproactive.rmi.port=1234 org.objectweb.proactive.examples.scheduler.HelloProActiveTask 

echo

