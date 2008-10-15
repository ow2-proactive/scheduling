@echo off
echo. 
echo --- Scheduler ---------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

rem This program will enable you to launch the scheduler daemon

%JAVA_CMD% org.ow2.proactive.scheduler.examples.SchedulerStarter  %*
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
