@echo off
echo. 
echo --- Create DataBase----------------------------------------------

rem --- Verifying current directory
SET COMMAND=%0
IF NOT "%COMMAND:~-4%" == ".bat" (
 SET COMMAND=%0.bat
)
 
SET OK=0
FOR /F %%i in ('dir /b') do IF "%%i" == "%COMMAND%" SET OK=1

IF %OK% == 0 (
echo scripts must be started in the same directory as the script.
goto end
)

goto doit

:usage
echo. 
goto end
:doit

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
