@echo off
echo.

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat cli

%JAVA_CMD% org.ow2.proactive.resourcemanager.utils.AdminRM %*

ENDLOCAL

:end
echo.

