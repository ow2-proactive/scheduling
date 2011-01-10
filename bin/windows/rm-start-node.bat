@echo off

if "%1" == "" goto usage

goto doit

:usage
echo.
echo using default values. -h to display help message.
echo.
goto doit

:doit
echo --- StartNode----------------------------------------
echo.

set ERRORLEVEL=

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat log4j-defaultNode
%JAVA_CMD%  org.ow2.proactive.resourcemanager.utils.RMNodeStarter %*
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------

exit /B %ERRORLEVEL%
