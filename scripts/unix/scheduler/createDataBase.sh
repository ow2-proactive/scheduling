#!/bin/sh

echo
echo --- Create DataBase----------------------------------------------


  CONFIG_FILE=$1

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

CLASSPATH=$workingDir/../../scheduler-plugins-src/org.objectweb.proactive.scheduler.plugin/bin/:$CLASSPATH

echo $JAVACMD

if [ -e "$1" ]; then
	echo "Copying $CONFIG_FILE to $PROACTIVE/classes/Extra/org/objectweb/proactive/extra/scheduler/util/db.cfg"
	cp $CONFIG_FILE $PROACTIVE/classes/Extra/org/objectweb/proactive/extra/scheduler/util/db.cfg
	$JAVACMD org.objectweb.proactive.extra.scheduler.util.CreateDataBase	
else
	echo "You must give a config file to create database ! Use the example db.cfg."
fi
echo
