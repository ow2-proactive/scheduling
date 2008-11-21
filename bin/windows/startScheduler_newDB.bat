@echo off
echo.
echo --- Create Database ---------------------------------------------

IF EXIST SCHEDULER_DB (
    RMDIR /S /Q SCHEDULER_DB
)
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

%JAVA_CMD% org.ow2.proactive.scheduler.core.db.CreateDataBase ..\..\config\database\scheduler_db.cfg

echo. 
echo --- Scheduler ---------------------------------------------

rem This program will enable you to launch the scheduler daemon

%JAVA_CMD% org.ow2.proactive.scheduler.examples.SchedulerStarter  %*
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------

