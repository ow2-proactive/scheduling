@echo off
echo. 
echo --- Job Launcher -------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD% org.objectweb.proactive.extensions.scheduler.examples.JobLauncher %1 %2 %3 %4

:end
echo. 
echo ---------------------------------------------------------
