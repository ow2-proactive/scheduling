#!/bin/sh

echo
echo --- Binary Tree ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh

$JAVACMD org.objectweb.proactive.examples.binarytree.TreeApplet

echo
echo ---------------------------------------------------------
