#!/bin/sh

workingDir=`dirname $0`
. $workingDir/env.sh

while true
do 
	echo
	echo --- StartP2PService -------------------------------------
   	nice $JAVACMD org.objectweb.proactive.p2p.core.service.StartService $@
   	
    # JVM Killed -9 or Crtl-C or wrong usage
    case $? in
	69 | 9 | 130 ) exit 0
    esac
    echo
	echo ---------------------------------------------------------
done
