@echo off
echo. 
echo --- Scheduler ---------------------------------------------

  SET RM_URL=%1


SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

rem This program will enable you to launch the scheduler daemon

%JAVA_CMD% -Xms128m -Xmx2048m org.objectweb.proactive.extensions.scheduler.examples.LocalSchedulerExample  %RM_URL%
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
