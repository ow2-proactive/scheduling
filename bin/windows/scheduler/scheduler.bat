@echo off
echo. 
echo --- Scheduler ---------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

rem This program will enable you to launch the scheduler daemon

%JAVA_CMD% org.objectweb.proactive.extensions.scheduler.examples.LocalSchedulerExample  %*
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
