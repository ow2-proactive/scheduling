@echo off
echo. 
echo --- Job Launcher -------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat


set CLASSPATH=%PAS_CLASSPATH%;%CLASSPATH%

%JAVA_CMD% org.ow2.proactive.scheduler.examples.JobLauncher %*

:end
echo. 
echo ---------------------------------------------------------
