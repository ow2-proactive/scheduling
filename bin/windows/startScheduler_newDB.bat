@echo off
echo.
echo --- Create Database ---------------------------------------------

IF EXIST SCHEDULER_DB (
    RMDIR /S /Q SCHEDULER_DB
    RM derby.log
)
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

echo. 
echo --- Scheduler ---------------------------------------------

%JAVA_CMD% org.ow2.proactive.scheduler.examples.SchedulerStarter  %*
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------

