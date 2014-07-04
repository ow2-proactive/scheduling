@echo off
echo.

set ERRORLEVEL=

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat scheduler-log4j-server
%JAVA_CMD% -Xms128m -Xmx1048m org.ow2.proactive.scheduler.util.SchedulerStarter %*
ENDLOCAL

:end
echo.

exit /B %ERRORLEVEL%
