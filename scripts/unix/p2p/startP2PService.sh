#!/bin/sh

workingDir=`dirname $0`

cd $PWD/..
. env.sh
cd $workingDir

#FT
if [ "$1" = "-ft" ]; then
	#-ft server_global ttc protocol
    shift
    FT="-Dproactive.ft=enable -Dproactive.ft.server.global=$1  -Dproactive.ft.ttc=$2 -Dproactive.ft.protocol=$3"
    shift
    shift
    shift
fi

while true
do 
	echo
	echo --- StartP2PService -------------------------------------
   	$JAVACMD $FT org.objectweb.proactive.p2p.service.StartP2PService "$@"
   	
    # JVM Killed -9 or Crtl-C or wrong usage
    case $? in
	1 | 9 | 130 ) exit 0
    esac
    echo
	echo ---------------------------------------------------------
done
