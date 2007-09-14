#!/bin/sh

export PROACTIVE=/user/cvergoni/home/NOSAVE/oasis/trunk/  
export JAVA_HOME=/user/cvergoni/home/jdk1.6.0_01/
cd /user/cvergoni/home/NOSAVE/oasis/trunk/scripts/unix/
source ./env.sh

cd -

$JAVACMD  org.objectweb.proactive.extra.p2pTest.p2p.StartP2P P2PDescriptor.xml
