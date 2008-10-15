#!/bin/sh

echo
echo --- Create DataBase----------------------------------------------

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh


if [ -e "$1" ]; then
	$JAVACMD org.ow2.proactive.scheduler.util.CreateDataBase $1
else
	echo "You must give a configuration file to create database ! Use scheduler_db.cfg as exemple."
fi
echo
