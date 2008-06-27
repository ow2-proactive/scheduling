@echo off
echo
echo --- Communicator ----------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD% org.objectweb.proactive.extensions.scheduler.examples.AdminCommunicator %*

:end
echo. 
echo ---------------------------------------------------------