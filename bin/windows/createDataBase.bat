@echo off
echo. 
echo --- Create DataBase----------------------------------------------


SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

SET CONFIG_FILE=%1
SET NULL_STRING=

IF "%CONFIG_FILE%" NEQ "" (
	%JAVA_CMD% org.ow2.proactive.scheduler.core.db.CreateDataBase %CONFIG_FILE%
) ELSE (
	echo You must give a configuration file to create database ! Use scheduler_db.cfg in the config/database directory as example.
)
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------