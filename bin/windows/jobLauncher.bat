@echo off
echo. 
echo --- Job Launcher -------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

%JAVA_CMD% org.ow2.proactive.scheduler.examples.JobLauncher %*
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
