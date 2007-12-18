@echo off
echo. 
echo --- SCHEDULER STRESS TEST ---------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD% org.objectweb.proactive.extensions.scheduler.examples.SchedulerTester %*

:end
echo.
echo ---------------------------------------------------------
