@echo off
echo.
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat rm-log4j-server
%JAVA_CMD% org.ow2.proactive.resourcemanager.utils.RMStarter %*

ENDLOCAL

:end
echo.

