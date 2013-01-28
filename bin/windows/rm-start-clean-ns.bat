@echo off
echo.

rem Start rm without node sources but with all history

set ERRORLEVEL=

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat rm-log4j-server
%JAVA_CMD% -Dpa.rm.db.hibernate.dropdb.nodesources=true -Dderby.stream.error.file="%PA_SCHEDULER%\.logs\derby.log" org.ow2.proactive.resourcemanager.utils.RMStarter %*
ENDLOCAL

:end
echo.

exit /B %ERRORLEVEL%
