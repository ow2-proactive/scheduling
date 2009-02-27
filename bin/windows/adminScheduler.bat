@echo off
echo.

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat cli

%JAVA_CMD% org.ow2.proactive.scheduler.util.AdminScheduler %*
ENDLOCAL

:end
echo.
