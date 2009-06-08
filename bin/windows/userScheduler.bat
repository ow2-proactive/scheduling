@echo off
echo.

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat log4j-client

%JAVA_CMD% org.ow2.proactive.scheduler.common.util.userconsole.UserController %*
ENDLOCAL

:end
echo.
