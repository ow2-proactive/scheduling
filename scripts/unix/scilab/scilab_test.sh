#!/bin/sh


if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh
. $PROACTIVE/scripts/unix/scilab/scilab_env.sh

echo
echo --- Scilab Test ---

echo --- Test 1 ---
$JAVACMD org.objectweb.proactive.extensions.scilab.test.SciTest1

#echo --- Test 2 ---
#$JAVACMD org.objectweb.proactive.ext.scilab.test.SciTest2 ScilabVN ProActiveScilab.xml

#echo --- Test 3 ---
#$JAVACMD org.objectweb.proactive.ext.scilab.test.SciTest3 ScilabVN ProActiveScilab.xml 3

#echo --- Test Seq Mandel ---
#$JAVACMD org.objectweb.proactive.ext.scilab.test.SciTestSeqMandel 100 100 -2 2 -2 2 testMandelSeqIn testMandelSeqOut

#echo --- Test Par Mandel ---
#$JAVACMD org.objectweb.proactive.ext.scilab.test.SciTestParMandel ScilabVN ProActiveScilab.xml 5 100 100 -2 2 -2 2 testMandelParIn testMandelParOut

#echo --- Test Seq Pi ---
#$JAVACMD org.objectweb.proactive.ext.scilab.test.SciTestSeqPi testPiSeqIn testPiSeqOut

#echo --- Test Par Pi ---
#$JAVACMD org.objectweb.proactive.ext.scilab.test.SciTestParPi ScilabVN ProActiveScilab.xml 5  testPiParIn testPiParOut

#echo --- Test Seq Mult ---
#$JAVACMD -Xmx128M org.objectweb.proactive.ext.scilab.test.SciTestSeqMult testMultSeqIn testMultSeqOut

#echo --- Test Par Mult ---
#$JAVACMD -Xmx1024M org.objectweb.proactive.ext.scilab.test.SciTestParMult ScilabVN ProActiveScilab.xml 5  testMultParIn testMultParOut

#echo --- Test 4 ---
#$JAVACMD org.objectweb.proactive.ext.scilab.test.SciTest4 testMultSeqIn testMultSeqOut

#echo --- Test Calcium ---
#$JAVACMD org.objectweb.proactive.ext.scilab.test.SciTestCalcium ScilabVN ProActiveScilab.xml


#echo --- Test Library ---
#$JAVACMD org.objectweb.proactive.ext.scilab.test.SciTestLibrary ScilabVN ProActiveScilab.xml 5 /tmp/listfun.sci /tmp/listfun1.sci

echo 
echo ------------------------------------------------------------
