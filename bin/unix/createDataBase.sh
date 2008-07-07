#!/bin/sh

echo
echo --- Create DataBase----------------------------------------------


  CONFIG_FILE=$1

workingDir=`pwd`
CLASSPATH=.
. ./env.sh


if [ -e "$1" ]; then
	$JAVACMD org.ow2.proactive.scheduler.util.CreateDataBase $CONFIG_FILE
else
	echo "You must give a configuration file to create database ! Use scheduler_db.cfg as exemple."
fi
echo
