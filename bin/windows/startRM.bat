@echo off
echo.

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

%JAVA_CMD% org.ow2.proactive.resourcemanager.utils.RMStarter %*

ENDLOCAL

:end
echo.

