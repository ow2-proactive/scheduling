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
echo Copying %CONFIG_FILE% to %PROACTIVE%\classes\Extensions\org\objectweb\proactive\extensions\scheduler\util\db.cfg
	cp %CONFIG_FILE% %PROACTIVE%\classes\Extensions\org\objectweb\proactive\extensions\scheduler\util\db.cfg
	%JAVA_CMD% org.objectweb.proactive.extensions.scheduler.util.CreateDataBase
) ELSE (
echo You must give a config file to create database ! Use the example db.cfg.
)
