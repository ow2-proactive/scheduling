@echo off
echo
echo --- Communicator ----------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD% -Xms128m -Xmx2048m org.objectweb.proactive.extensions.scheduler.examples.AdminCommunicator %1 %2 %3

:end
echo. 
echo ---------------------------------------------------------