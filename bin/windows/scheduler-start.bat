@echo off
echo.

set ERRORLEVEL=

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat scheduler-log4j-server
%JAVA_CMD% -Dderby.stream.error.file="%PA_SCHEDULER%\.logs\derby.log" -Dproactive.pamr.agent.id=1 -Dproactive.pamr.agent.magic_cookie=scheduler -Xms128m -Xmx1048m org.ow2.proactive.scheduler.util.SchedulerStarter %*
ENDLOCAL

:end
echo.

exit /B %ERRORLEVEL%
