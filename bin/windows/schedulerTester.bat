@echo off
echo. 
echo --- SCHEDULER STRESS TEST ---------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

%JAVA_CMD% org.ow2.proactive.scheduler.examples.SchedulerTester %*

:end
echo.
echo ---------------------------------------------------------
