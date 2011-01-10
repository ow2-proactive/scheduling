@echo off
echo.

set ERRORLEVEL=

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat log4j-client
%JAVA_CMD% -Djava.awt.headless=true org.ow2.proactive.resourcemanager.utils.console.ResourceManagerController %*
ENDLOCAL

:end
echo.

exit /B %ERRORLEVEL%
