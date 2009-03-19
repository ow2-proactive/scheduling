@echo off
echo.

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat client

%JAVA_CMD% org.ow2.proactive.scheduler.common.util.JobLauncher %*
ENDLOCAL

:end
echo.
