#!/bin/sh

workingDir=`dirname $0`

cd $PWD/../../../scripts/unix
. env.sh
cd $workingDir

while true
do 
	echo
	echo --- StartP2PService -------------------------------------
   	nice $JAVACMD org.objectweb.proactive.p2p.core.service.StartP2PService $@
   	
    # JVM Killed -9 or Crtl-C or wrong usage
    case $? in
	1 | 9 | 130 ) exit 0
    esac
    echo
	echo ---------------------------------------------------------
done
