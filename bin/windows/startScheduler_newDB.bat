@echo off
echo.

IF EXIST SCHEDULER_DB (
    RMDIR /S /Q SCHEDULER_DB
    RM derby.log
)
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

echo. 

%JAVA_CMD% org.ow2.proactive.scheduler.examples.SchedulerStarter  %*
ENDLOCAL

:end
echo. 

