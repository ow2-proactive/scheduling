@echo off
echo
echo --- Communicator ----------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

%JAVA_CMD% org.ow2.proactive.scheduler.examples.AdminCommunicator %*
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
