@echo off
echo. 
echo --- Create DataBase----------------------------------------------


SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

SET CONFIG_FILE=%1
SET NULL_STRING=

SET CLASSPATH="%PROACTIVE%\scheduler-plugins-src\org.objectweb.proactive.scheduler.plugin\bin\:%CLASSPATH%

IF "%CONFIG_FILE%" NEQ "" (
	%JAVA_CMD% org.objectweb.proactive.extensions.scheduler.util.CreateDataBase %CONFIG_FILE%
) ELSE (
	echo You must give a configuration file to create database ! Use scheduler_db.cfg as exemple.
)
