@echo off
echo
echo --- Communicator ----------------------------------------------

  SET SCHEDULER_URL=%1

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD% org.objectweb.proactive.extensions.scheduler.examples.AdminCommunicator %SCHEDULER_URL%

:end
echo. 
echo ---------------------------------------------------------