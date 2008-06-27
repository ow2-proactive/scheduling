@echo off
echo. 
echo --- Job Launcher -------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

set CLASSPATH=%PAS_CLASSPATH%;%CLASSPATH%

%JAVA_CMD% org.objectweb.proactive.extensions.scheduler.examples.JobLauncher %*

:end
echo. 
echo ---------------------------------------------------------
