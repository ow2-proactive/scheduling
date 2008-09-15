@echo off
echo. 

RMDIR /S /Q SCHEDULER_DB
createDataBase.bat ..\..\config\database\scheduler_db.cfg

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

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

