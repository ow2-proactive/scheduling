@echo off
echo.

set ERRORLEVEL=

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat rm-log4j-server
%JAVA_CMD% -Dpa.rm.db.hibernate.dropdb=true -Dderby.stream.error.file="%PA_SCHEDULER%\.logs\derby.log" -Dproactive.pamr.agent.id=0 -Dproactive.pamr.agent.magic_cookie=rm org.ow2.proactive.resourcemanager.utils.RMStarter %*
ENDLOCAL

:end
echo.

exit /B %ERRORLEVEL%
