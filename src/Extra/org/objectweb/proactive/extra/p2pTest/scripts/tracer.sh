#!/bin/sh

export PROACTIVE=/user/cvergoni/home/NOSAVE/oasis/trunk/  
export JAVA_HOME=/user/cvergoni/home/jdk1.6.0_01/
cd /user/cvergoni/home/NOSAVE/oasis/trunk/scripts/unix/
source ./env.sh

cd  -

if [ "$#" = "0" ]; then 
    echo $0 " adresse opt" 
    echo "   adresse: ex://fiacre.inria.fr/"
    echo "   opt: dump trace reqnode"
else
    $JAVACMD org.objectweb.proactive.extra.p2pTest.p2p.Tracer $1 $2
fi
